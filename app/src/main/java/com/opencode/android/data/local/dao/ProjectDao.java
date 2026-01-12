package com.opencode.android.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.opencode.android.data.local.entity.ProjectEntity;

import java.util.List;

/**
 * Data Access Object for Project operations.
 */
@Dao
public interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProjectEntity project);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProjectEntity> projects);

    @Update
    void update(ProjectEntity project);

    @Delete
    void delete(ProjectEntity project);

    @Query("DELETE FROM projects WHERE id = :projectId")
    void deleteById(String projectId);

    @Query("DELETE FROM projects")
    void deleteAll();

    @Query("SELECT * FROM projects WHERE id = :projectId")
    LiveData<ProjectEntity> getById(String projectId);

    @Query("SELECT * FROM projects WHERE id = :projectId")
    ProjectEntity getByIdSync(String projectId);

    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    LiveData<List<ProjectEntity>> getAll();

    @Query("SELECT * FROM projects WHERE is_template = 0 ORDER BY updated_at DESC")
    LiveData<List<ProjectEntity>> getAllProjects();

    @Query("SELECT * FROM projects WHERE is_template = 1 ORDER BY updated_at DESC")
    LiveData<List<ProjectEntity>> getAllTemplates();

    @Query("SELECT * FROM projects ORDER BY updated_at DESC LIMIT :limit")
    LiveData<List<ProjectEntity>> getRecent(int limit);

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<ProjectEntity>> search(String query);

    @Query("SELECT * FROM projects WHERE language = :language ORDER BY updated_at DESC")
    LiveData<List<ProjectEntity>> getByLanguage(String language);

    @Query("SELECT * FROM projects WHERE session_id = :sessionId")
    LiveData<List<ProjectEntity>> getBySessionId(String sessionId);

    @Query("SELECT * FROM projects WHERE session_id = :sessionId")
    ProjectEntity getBySessionIdSync(String sessionId);

    @Query("SELECT COUNT(*) FROM projects")
    LiveData<Integer> getCount();

    @Query("SELECT COUNT(*) FROM projects WHERE is_template = 0")
    LiveData<Integer> getProjectCount();

    @Query("UPDATE projects SET name = :name WHERE id = :projectId")
    void updateName(String projectId, String name);

    @Query("UPDATE projects SET file_count = file_count + 1 WHERE id = :projectId")
    void incrementFileCount(String projectId);

    @Query("UPDATE projects SET file_count = file_count - 1 WHERE id = :projectId")
    void decrementFileCount(String projectId);

    @Query("UPDATE projects SET language = :language WHERE id = :projectId")
    void updateLanguage(String projectId, String language);

    @Query("SELECT * FROM projects ORDER BY updated_at DESC LIMIT 1")
    ProjectEntity getMostRecentSync();
}
