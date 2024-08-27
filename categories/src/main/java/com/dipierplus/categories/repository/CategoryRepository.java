package com.dipierplus.categories.repository;

import com.dipierplus.categories.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
