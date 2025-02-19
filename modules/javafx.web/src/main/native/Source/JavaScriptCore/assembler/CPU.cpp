/*
 * Copyright (C) 2019-2023 Apple Inc. All rights reserved.
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
 * THIS SOFTWARE IS PROVIDED BY APPLE INC. ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL APPLE INC. OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include "config.h"
#include "CPU.h"

#if (CPU(X86) || CPU(X86_64) || CPU(ARM64)) && OS(DARWIN)
#include <mutex>
#include <sys/sysctl.h>
#endif

#if ENABLE(ASSEMBLER)
#include "MacroAssembler.h"
#endif

namespace JSC {

#if PLATFORM(MAC) || PLATFORM(MACCATALYST)
bool isKernOpenSource()
{
    uint32_t val = 0;
    size_t valSize = sizeof(val);
    return !sysctlbyname("kern.opensource_kernel", &val, &valSize, nullptr, 0) && val;
}
#endif

#if (CPU(X86) || CPU(X86_64)) && OS(DARWIN)
bool isKernTCSMAvailable()
{
    if (!Options::useKernTCSM())
        return false;

    uint32_t val = 0;
    size_t valSize = sizeof(val);
    int rc = sysctlbyname("kern.tcsm_available", &val, &valSize, nullptr, 0);
    if (rc < 0)
        return false;
    return !!val;
}

bool enableKernTCSM()
{
    uint32_t val = 1;
    int rc = sysctlbyname("kern.tcsm_enable", nullptr, nullptr, &val, sizeof(val));
    if (rc < 0)
        return false;
    return true;
}

int kernTCSMAwareNumberOfProcessorCores()
{
    static std::once_flag onceFlag;
    static int result;
    std::call_once(onceFlag, [] {
        result = WTF::numberOfProcessorCores();
        if (result <= 1)
            return;
        if (isKernTCSMAvailable())
            --result;
    });
    return result;
}

int64_t hwL3CacheSize()
{
    int64_t val = 0;
    size_t valSize = sizeof(val);
    int rc = sysctlbyname("hw.l3cachesize", &val, &valSize, nullptr, 0);
    if (rc < 0)
        return 0;
    return val;
}

int32_t hwPhysicalCPUMax()
{
    int32_t val = 0;
    size_t valSize = sizeof(val);
    int rc = sysctlbyname("hw.physicalcpu_max", &val, &valSize, nullptr, 0);
    if (rc < 0)
        return 0;
    return val;
}

#endif // #if (CPU(X86) || CPU(X86_64)) && OS(DARWIN)

#if CPU(ARM64) && !(CPU(ARM64E) || OS(MAC_OS_X))
bool isARM64_LSE()
{
#if ENABLE(ASSEMBLER)
    return MacroAssembler::supportsLSE();
#else
    return false;
#endif
}
#endif

#if CPU(ARM64E)
bool isARM64E_FPAC()
{
#if OS(DARWIN)
    static std::once_flag onceFlag;
    std::call_once(onceFlag, [] {
        uint32_t val = 0;
        size_t valSize = sizeof(val);
        int rc = sysctlbyname("hw.optional.arm.FEAT_FPAC", &val, &valSize, nullptr, 0);
        g_jscConfig.canUseFPAC = rc < 0 ? false : !!val;
    });
    return g_jscConfig.canUseFPAC;
#else
    return false;
#endif
}
#endif // CPU(ARM64E)

#if CPU(X86_64)
bool isX86_64_AVX()
{
#if ENABLE(ASSEMBLER)
    return MacroAssembler::supportsAVX();
#else
    return false;
#endif
}
#endif

} // namespace JSC
