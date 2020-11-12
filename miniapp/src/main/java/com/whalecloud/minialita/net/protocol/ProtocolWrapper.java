package com.whalecloud.minialita.net.protocol;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ProtocolWrapper {

    String getBaseUrl();

    String getUrl();

    String getSavePath();

    Map<String, Object> getParams();

    Map<String, String> getHeaders();

    Map<String, List<File>> getFileMap();
}
