package com.medicology.dictionary;

import com.medicology.dictionary.config.DictionaryAiProperties;
import com.medicology.dictionary.config.DictionaryAssetProperties;
import com.medicology.dictionary.entity.Article;
import com.medicology.dictionary.repository.ArticleRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = ArticleRepository.class)
@EntityScan(basePackageClasses = Article.class)
@EnableConfigurationProperties({DictionaryAiProperties.class, DictionaryAssetProperties.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
