package com.jug.util;

import com.jug.datahandling.Version;

public interface IVersionProvider {
    String getVersionString();

    Version getVersion();
}
