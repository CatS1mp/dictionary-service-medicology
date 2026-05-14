package com.medicology.dictionary.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "dictionary.assets")
public class DictionaryAssetProperties {
    private String baseUrl = "/api/dictionary/assets";
    private String storagePath = "uploads/dictionary-assets";
    private long maxSizeBytes = 5 * 1024 * 1024;
    private List<String> allowedTypes = List.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif");

    /**
     * When {@code url}, {@code bucket} and {@code serviceRoleKey} are all non-blank, uploads go to Supabase Storage
     * and the response {@code url} is the public object URL. Otherwise files are stored on local disk.
     */
    private Supabase supabase = new Supabase();

    @Getter
    @Setter
    public static class Supabase {
        private String url = "";
        private String bucket = "";
        private String serviceRoleKey = "";
        /** Folder inside the bucket, e.g. {@code dictionary-assets}. */
        private String objectPrefix = "dictionary-assets";
    }
}
