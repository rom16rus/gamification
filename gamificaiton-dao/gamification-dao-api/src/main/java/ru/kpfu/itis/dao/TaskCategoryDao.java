package ru.kpfu.itis.dao;

import ru.kpfu.itis.model.classifier.TaskCategory;

/**
 * Created by timur on 24.06.15.
 */
public interface TaskCategoryDao {

    TaskCategory findByName(String name);

    TaskCategory save(TaskCategory taskCategory);
}
