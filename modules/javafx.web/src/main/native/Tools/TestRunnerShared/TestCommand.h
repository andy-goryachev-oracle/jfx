/*
 * Copyright (C) 2020 Apple Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. AND ITS CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE INC. OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

#pragma once

#include <string>
#include <wtf/Seconds.h>

#if HAVE(STD_FILESYSTEM) || HAVE(STD_EXPERIMENTAL_FILESYSTEM)
#include <wtf/StdFilesystem.h>
#endif

namespace WTR {

struct TestCommand {
    std::string pathOrURL;
#if PLATFORM(JAVA)
// Requires macosx-min-version=10.15
    std::string absolutePath;
#else
    std::filesystem::path absolutePath;
#endif
    std::string expectedPixelHash;
    std::string selfComparisonHeader;
    std::string additionalHeader;
    WTF::Seconds timeout;
    bool shouldDumpPixels { false };
    bool forceDumpPixels { false };
    bool dumpJSConsoleLogInStdErr { false };
};

TestCommand parseInputLine(const std::string& inputLine);

#if PLATFORM(JAVA)
std::string testPath(const std::string& pathOrURL);
#else
std::filesystem::path testPath(const std::string& pathOrURL);
#endif
std::string testURLString(const std::string& pathOrURL);

}
