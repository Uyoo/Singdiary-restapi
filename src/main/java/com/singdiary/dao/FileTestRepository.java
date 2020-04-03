package com.singdiary.dao;

import com.singdiary.common.Description;
import org.springframework.stereotype.Repository;

@Repository
public interface FileTestRepository {

    @Description("파일 업로드")
    void uploadFile() throws Exception;
}
