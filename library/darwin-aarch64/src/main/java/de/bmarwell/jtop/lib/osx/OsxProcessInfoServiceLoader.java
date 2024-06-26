/*
 * Copyright (C) 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bmarwell.jtop.lib.osx;

import de.bmarwell.jtop.lib.api.spi.ProcessH;
import de.bmarwell.jtop.lib.api.spi.ProcessInfoServiceLoader;
import java.util.Locale;

public class OsxProcessInfoServiceLoader implements ProcessInfoServiceLoader {

    @Override
    public boolean isOs(String currentOs) {
        return currentOs.toLowerCase(Locale.ROOT).startsWith("mac os x");
    }

    public boolean isArch(String currentArch) {
        return "aarch64".equals(currentArch.toLowerCase(Locale.ROOT));
    }

    @Override
    public ProcessH newProcessH() {
        return new OsxProcessH();
    }
}
