package top.wjr.serenoj.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.entity.Problem;
import top.wjr.serenoj.pojo.entity.ProblemCase;
import top.wjr.serenoj.service.ProblemCaseService;
import top.wjr.serenoj.service.ProblemService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/file")
@RequiresPermissions("problem_admin")
public class FileController {

    private static final Pattern IN_FILE_PATTERN = Pattern.compile("^(.+)\\.in$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OUT_FILE_PATTERN = Pattern.compile("^(.+)\\.(out|ans)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INPUT_TXT_PATTERN = Pattern.compile("^input(\\d+)\\.txt$", Pattern.CASE_INSENSITIVE);
    private static final Pattern OUTPUT_TXT_PATTERN = Pattern.compile("^output(\\d+)\\.txt$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemCaseService problemCaseService;

    @PostMapping("/upload-testcase-zip")
    public CommonResult<Map<String, Object>> uploadTestcaseZip(@RequestParam("file") MultipartFile file,
                                                               @RequestParam(required = false) String mode)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("测试数据 zip 不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException("只支持上传 zip 文件");
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "serenoj", "data", "testcase", "tmp", uuid);
        Files.createDirectories(tempDir);

        Map<String, CasePair> caseMap = readZipCases(file, tempDir);
        if (caseMap.isEmpty()) {
            throw new IllegalArgumentException("zip 内未找到 .in/.out 测试数据");
        }

        List<ProblemCase> fileList = buildProblemCases(caseMap);
        Map<String, Object> data = new HashMap<>();
        data.put("fileListDir", "data/testcase/tmp/" + uuid);
        data.put("fileList", fileList);
        return CommonResult.successResponse(data);
    }

    @GetMapping("/download-testcase")
    public void downloadTestcase(@RequestParam Long pid, HttpServletResponse response) throws IOException {
        Problem problem = problemService.getById(pid);
        if (problem == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        List<ProblemCase> cases = problemCaseService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ProblemCase>()
                        .eq("pid", pid)
                        .eq("status", 0)
                        .orderByAsc("group_num", "id"));
        if (cases.isEmpty()) {
            throw new IllegalArgumentException("该题目暂无测试数据");
        }

        String zipName = problem.getProblemId() + "-testcase.zip";
        String encodedName = URLEncoder.encode(zipName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.reset();
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedName);

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
            int index = 1;
            for (ProblemCase testcase : cases) {
                writeZipEntry(zipOut, index + ".in", testcase.getInput());
                writeZipEntry(zipOut, index + ".out", testcase.getOutput());
                index++;
            }
        }
    }

    private Map<String, CasePair> readZipCases(MultipartFile file, Path tempDir) throws IOException {
        Map<String, CasePair> caseMap = new LinkedHashMap<>();
        try (ZipInputStream zipInput = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInput.closeEntry();
                    continue;
                }

                String safeName = getSafeFilename(entry.getName());
                CaseFile caseFile = parseCaseFilename(safeName);
                if (caseFile == null) {
                    zipInput.closeEntry();
                    continue;
                }

                byte[] content = readEntryBytes(zipInput);
                Files.write(tempDir.resolve(safeName), content);
                CasePair pair = caseMap.computeIfAbsent(caseFile.key, CasePair::new);
                String text = new String(content, StandardCharsets.UTF_8);
                if (caseFile.input) {
                    pair.input = text;
                } else {
                    pair.output = text;
                }
                zipInput.closeEntry();
            }
        }
        return caseMap;
    }

    private List<ProblemCase> buildProblemCases(Map<String, CasePair> caseMap) {
        List<CasePair> pairs = new ArrayList<>(caseMap.values());
        pairs.sort((left, right) -> compareCaseKey(left.key, right.key));

        int count = pairs.size();
        int baseScore = count == 0 ? 0 : 100 / count;
        int remainder = count == 0 ? 0 : 100 - baseScore * count;

        List<ProblemCase> cases = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) {
            CasePair pair = pairs.get(i);
            if (pair.input == null || pair.output == null) {
                throw new IllegalArgumentException("测试点 " + pair.key + " 缺少输入或输出文件");
            }
            ProblemCase problemCase = new ProblemCase();
            problemCase.setInput(pair.input);
            problemCase.setOutput(pair.output);
            problemCase.setScore(baseScore + (i >= count - remainder ? 1 : 0));
            problemCase.setStatus(0);
            problemCase.setGroupNum(i + 1);
            cases.add(problemCase);
        }
        return cases;
    }

    private CaseFile parseCaseFilename(String filename) {
        Matcher matcher = IN_FILE_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return new CaseFile(matcher.group(1), true);
        }
        matcher = OUT_FILE_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return new CaseFile(matcher.group(1), false);
        }
        matcher = INPUT_TXT_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return new CaseFile(matcher.group(1), true);
        }
        matcher = OUTPUT_TXT_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return new CaseFile(matcher.group(1), false);
        }
        return null;
    }

    private String getSafeFilename(String entryName) {
        String normalized = entryName.replace("\\", "/");
        String filename = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (filename.isEmpty() || filename.startsWith(".")) {
            throw new IllegalArgumentException("zip 内包含非法文件名");
        }
        return filename;
    }

    private byte[] readEntryBytes(ZipInputStream zipInput) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int length;
        while ((length = zipInput.read(buffer)) != -1) {
            output.write(buffer, 0, length);
        }
        return output.toByteArray();
    }

    private void writeZipEntry(ZipOutputStream zipOut, String name, String content) throws IOException {
        zipOut.putNextEntry(new ZipEntry(name));
        if (content != null) {
            zipOut.write(content.getBytes(StandardCharsets.UTF_8));
        }
        zipOut.closeEntry();
    }

    private int compareCaseKey(String left, String right) {
        Integer leftNumber = parseInteger(left);
        Integer rightNumber = parseInteger(right);
        if (leftNumber != null && rightNumber != null) {
            return leftNumber.compareTo(rightNumber);
        }
        return left.compareTo(right);
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class CaseFile {
        private final String key;
        private final boolean input;

        private CaseFile(String key, boolean input) {
            this.key = key;
            this.input = input;
        }
    }

    private static class CasePair {
        private final String key;
        private String input;
        private String output;

        private CasePair(String key) {
            this.key = key;
        }
    }
}
