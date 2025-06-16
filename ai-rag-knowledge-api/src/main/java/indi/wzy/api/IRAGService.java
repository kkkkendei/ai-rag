package indi.wzy.api;

import org.springframework.web.multipart.MultipartFile;
import response.Response;

import java.util.List;

/**
 * @author wuzeyu
 * @description rag知识库服务接口
 * @github github.com/kkkkendei
 */
public interface IRAGService {

    Response<List<String>> queryRagTagList();

    Response<String > uploadFile(String ragId, List<MultipartFile> files);

    Response<String> analyzeGitRepository(String repoUrl, String userName, String token) throws Exception;

}
