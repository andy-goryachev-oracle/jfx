/*
 * Copyright (C) 2012 Google Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "config.h"
#include "WebSocketDeflateFramer.h"

#include "WebSocketExtensionProcessor.h"
#include "WebSocketFrame.h"
#include <wtf/HashMap.h>
#include <wtf/text/StringHash.h>
#include <wtf/text/StringToIntegerConversion.h>

namespace WebCore {

class WebSocketExtensionDeflateFrame final : public WebSocketExtensionProcessor {
    WTF_MAKE_FAST_ALLOCATED;
public:
    explicit WebSocketExtensionDeflateFrame(WebSocketDeflateFramer&);

private:
    String handshakeString() final;
    bool processResponse(const HashMap<String, String>&) final;
    String failureReason() final { return m_failureReason; }
#if !PLATFORM(JAVA)
    WebSocketDeflateFramer& m_framer;
    bool m_responseProcessed { false };
#endif
    String m_failureReason;
};

// FXIME: Remove vendor prefix after the specification matured.
WebSocketExtensionDeflateFrame::WebSocketExtensionDeflateFrame(WebSocketDeflateFramer& framer)
    : WebSocketExtensionProcessor("x-webkit-deflate-frame"_s)
#if !PLATFORM(JAVA)
    , m_framer(framer)
#endif
{
#if PLATFORM(JAVA)
     UNUSED_PARAM(framer);
#endif
}

String WebSocketExtensionDeflateFrame::handshakeString()
{
    return extensionToken(); // No parameter
}

bool WebSocketExtensionDeflateFrame::processResponse(const HashMap<String, String>& serverParameters)
{
#if !PLATFORM(JAVA)
    if (m_responseProcessed) {
        m_failureReason = "Received duplicate deflate-frame response"_s;
        return false;
    }
    m_responseProcessed = true;

    unsigned expectedNumParameters = 0;
    int windowBits = 15;
    auto parameter = serverParameters.find<HashTranslatorASCIILiteral>("max_window_bits"_s);
    if (parameter != serverParameters.end()) {
        windowBits = parseIntegerAllowingTrailingJunk<int>(parameter->value).value_or(0);
        if (windowBits < 8 || windowBits > 15) {
            m_failureReason = "Received invalid max_window_bits parameter"_s;
            return false;
        }
        expectedNumParameters++;
    }

    WebSocketDeflater::ContextTakeOverMode mode = WebSocketDeflater::TakeOverContext;
    parameter = serverParameters.find<HashTranslatorASCIILiteral>("no_context_takeover"_s);
    if (parameter != serverParameters.end()) {
        if (!parameter->value.isNull()) {
            m_failureReason = "Received invalid no_context_takeover parameter"_s;
            return false;
        }
        mode = WebSocketDeflater::DoNotTakeOverContext;
        expectedNumParameters++;
    }

    if (expectedNumParameters != serverParameters.size()) {
        m_failureReason = "Received unexpected deflate-frame parameter"_s;
        return false;
    }

    m_framer.enableDeflate(windowBits, mode);
    return true;
#else
    UNUSED_PARAM(serverParameters);
    return false;
#endif
}

DeflateResultHolder::DeflateResultHolder(WebSocketDeflateFramer& framer)
    : m_framer(framer)
{
}

DeflateResultHolder::~DeflateResultHolder()
{
    m_framer.resetDeflateContext();
}

void DeflateResultHolder::fail(const String& failureReason)
{
    m_succeeded = false;
    m_failureReason = failureReason;
}

InflateResultHolder::InflateResultHolder(WebSocketDeflateFramer& framer)
    : m_framer(framer)
{
}

InflateResultHolder::~InflateResultHolder()
{
    m_framer.resetInflateContext();
}

void InflateResultHolder::fail(const String& failureReason)
{
    m_succeeded = false;
    m_failureReason = failureReason;
}

std::unique_ptr<WebSocketExtensionProcessor> WebSocketDeflateFramer::createExtensionProcessor()
{
    return makeUnique<WebSocketExtensionDeflateFrame>(*this);
}

#if !PLATFORM(JAVA)
void WebSocketDeflateFramer::enableDeflate(int windowBits, WebSocketDeflater::ContextTakeOverMode mode)
{
    m_deflater = makeUnique<WebSocketDeflater>(windowBits, mode);
    m_inflater = makeUnique<WebSocketInflater>();
    if (!m_deflater->initialize() || !m_inflater->initialize()) {
        m_deflater = nullptr;
        m_inflater = nullptr;
        return;
    }
    m_enabled = true;
}
#endif

std::unique_ptr<DeflateResultHolder> WebSocketDeflateFramer::deflate(WebSocketFrame& frame)
{
#if !PLATFORM(JAVA)
    auto result = makeUnique<DeflateResultHolder>(*this);
    if (!enabled() || !WebSocketFrame::isNonControlOpCode(frame.opCode) || !frame.payload.size())
        return result;
    if (!m_deflater->addBytes(frame.payload) || !m_deflater->finish()) {
        result->fail("Failed to compress frame"_s);
        return result;
    }
    frame.compress = true;
    frame.payload = m_deflater->span();
    return result;
#else
    UNUSED_PARAM(frame);
    return makeUnique<DeflateResultHolder>(*this);
#endif
}

void WebSocketDeflateFramer::resetDeflateContext()
{
#if !PLATFORM(JAVA)
    if (m_deflater)
        m_deflater->reset();
#endif
}

std::unique_ptr<InflateResultHolder> WebSocketDeflateFramer::inflate(WebSocketFrame& frame)
{
    auto result = makeUnique<InflateResultHolder>(*this);
    if (!enabled() && frame.compress) {
        result->fail("Compressed bit must be 0 if no negotiated deflate-frame extension"_s);
        return result;
    }
#if !PLATFORM(JAVA)
    if (!frame.compress)
        return result;
    if (!WebSocketFrame::isNonControlOpCode(frame.opCode)) {
        result->fail("Received unexpected compressed frame"_s);
        return result;
    }
    if (!m_inflater->addBytes(frame.payload) || !m_inflater->finish()) {
        result->fail("Failed to decompress frame"_s);
        return result;
    }
    frame.compress = false;
    frame.payload = m_inflater->span();
    return result;
#else
    return result;
#endif
}

void WebSocketDeflateFramer::resetInflateContext()
{
#if !PLATFORM(JAVA)
    if (m_inflater)
        m_inflater->reset();
#endif
}

void WebSocketDeflateFramer::didFail()
{
    resetDeflateContext();
    resetInflateContext();
}

} // namespace WebCore
