package com.cheng.auth.controller;

import com.cheng.auth.service.AuthService;
import com.cheng.auth.service.VerificationCodeService;
import com.cheng.common.dto.CommonResponse;
import com.cheng.common.dto.LoginRequest;
import com.cheng.common.dto.LoginResponse;
import com.cheng.common.dto.RegisterRequest;
import com.cheng.common.entity.User;
import com.cheng.common.entity.Wallet;
import com.cheng.common.repository.UserRepository;
import com.cheng.common.repository.WalletRepository;
import com.cheng.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return CommonResponse.success(response);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 用户注册（支持 @RequestParam）
     */
    @PostMapping("/register")
    public CommonResponse<Map<String, Object>> register(
            @RequestParam String username,
            @RequestParam String password) {
        try {
            User user = authService.register(username, password, null, null);

            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("points", points);
            userInfo.put("balance", balance);

            return CommonResponse.success(userInfo);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 用户注册（支持 @RequestBody）
     */
    @PostMapping("/register/body")
    public CommonResponse<Map<String, Object>> registerWithBody(@RequestBody RegisterRequest request) {
        try {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return CommonResponse.error(400, "两次输入的密码不一致");
            }

            User user = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getPhone(),
                request.getEmail()
            );

            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("phone", user.getPhone());
            userInfo.put("email", user.getEmail());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("points", points);
            userInfo.put("balance", balance);

            return CommonResponse.success(userInfo);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 邮箱验证码注册
     */
    @PostMapping("/register-with-email")
    public CommonResponse<Map<String, Object>> registerWithEmail(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            String code = request.get("code");

            if (username == null || username.trim().isEmpty()) {
                return CommonResponse.error(400, "用户名不能为空");
            }
            if (password == null || password.length() < 6) {
                return CommonResponse.error(400, "密码长度至少6位");
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return CommonResponse.error(400, "邮箱格式不正确");
            }
            if (code == null || code.length() != 6) {
                return CommonResponse.error(400, "验证码格式不正确");
            }

            verificationCodeService.verifyCode("email", email, code);

            if (userRepository.findByUsername(username).isPresent()) {
                return CommonResponse.error(400, "用户名已存在");
            }

            if (userRepository.findByEmail(email).isPresent()) {
                return CommonResponse.error(400, "该邮箱已被注册");
            }

            User user = authService.register(username, password, null, email);

            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("createdAt", user.getCreatedAt());
            userInfo.put("points", points);
            userInfo.put("balance", balance);

            return CommonResponse.success(userInfo);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public CommonResponse<Map<String, Object>> getUserInfo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }

            String token = authorization.substring(7);

            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            Wallet wallet = walletRepository.findById(userId).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("email", user.getEmail());
            userInfo.put("phone", user.getPhone());
            userInfo.put("signature", user.getSignature());
            userInfo.put("avatarUrl", user.getAvatarUrl());
            userInfo.put("points", points);
            userInfo.put("balance", balance);

            return CommonResponse.success(userInfo);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public CommonResponse<Map<String, Object>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }

            String token = authorization.substring(7);

            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String newToken = JwtUtil.generateToken(user.getId(), user.getUsername());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String expiry = sdf.format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));

            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("expiry", expiry);

            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public CommonResponse<Void> logout() {
        return CommonResponse.success(null);
    }

    /**
     * 搜索用户（用于好友系统）
     */
    @GetMapping("/search")
    public CommonResponse<Map<String, Object>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 按ID精确搜索
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    return CommonResponse.error("用户不存在");
                }
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatarUrl", user.getAvatarUrl());
                userInfo.put("signature", user.getSignature());
                userInfo.put("userLevel", user.getUserLevel());
                
                Map<String, Object> result = new HashMap<>();
                result.put("list", java.util.Collections.singletonList(userInfo));
                result.put("total", 1);
                return CommonResponse.success(result);
            }
            
            // 按关键词模糊搜索（用户名或昵称）
            if (keyword == null || keyword.trim().isEmpty()) {
                return CommonResponse.error("请提供搜索关键词或用户ID");
            }
            
            String searchPattern = "%" + keyword + "%";
            org.springframework.data.domain.Page<User> users = userRepository.findByUsernameContainingOrNicknameContaining(
                searchPattern, searchPattern,
                org.springframework.data.domain.PageRequest.of(page - 1, size)
            );
            
            java.util.List<Map<String, Object>> userList = new java.util.ArrayList<>();
            for (User user : users.getContent()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatarUrl", user.getAvatarUrl());
                userInfo.put("signature", user.getSignature());
                userInfo.put("userLevel", user.getUserLevel());
                userList.add(userInfo);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", userList);
            result.put("total", users.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            
            return CommonResponse.success(result);
        } catch (Exception e) {
            return CommonResponse.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public CommonResponse<Map<String, Object>> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }

            String token = authorization.substring(7);
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            if (file.isEmpty()) {
                return CommonResponse.error(400, "请选择要上传的文件");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return CommonResponse.error(400, "只能上传图片文件");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return CommonResponse.error(400, "图片大小不能超过5MB");
            }

            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            String baseUrl = scheme + "://" + serverName;
            if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
                baseUrl += ":" + serverPort;
            }

            String avatarUrl = baseUrl + contextPath + "/uploads/avatars/" + filename;

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            Map<String, Object> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            result.put("url", avatarUrl);

            return CommonResponse.success(result);
        } catch (IOException e) {
            return CommonResponse.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 发送手机验证码
     */
    @PostMapping("/send-sms-code")
    public CommonResponse<Void> sendSmsCode(
            @RequestParam String phone,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return CommonResponse.error(400, "手机号格式不正确");
            }

            verificationCodeService.sendSmsCode(phone);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/send-email-code")
    public CommonResponse<Void> sendEmailCode(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "login") String purpose,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return CommonResponse.error(400, "邮箱格式不正确");
            }

            if (!"login".equals(purpose) && !"register".equals(purpose)) {
                return CommonResponse.error(400, "无效的用途参数");
            }

            verificationCodeService.sendEmailCode(email, purpose);
            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 绑定手机
     */
    @PostMapping("/bind-phone")
    public CommonResponse<Void> bindPhone(
            @RequestParam String phone,
            @RequestParam String code,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }

            String token = authorization.substring(7);
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            verificationCodeService.verifyCode("sms", phone, code);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setPhone(phone);
            user.setPhoneVerified(true);
            userRepository.save(user);

            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 绑定邮箱
     */
    @PostMapping("/bind-email")
    public CommonResponse<Void> bindEmail(
            @RequestParam String email,
            @RequestParam String code,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return CommonResponse.error(401, "未授权访问");
            }

            String token = authorization.substring(7);
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }

            verificationCodeService.verifyCode("email", email, code);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setEmail(email);
            user.setEmailVerified(true);
            userRepository.save(user);

            return CommonResponse.success(null);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }

    /**
     * 邮箱验证码登录
     */
    @PostMapping("/login-email")
    public CommonResponse<LoginResponse> loginWithEmail(
            @RequestParam String email,
            @RequestParam String code) {
        try {
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return CommonResponse.error(400, "邮箱格式不正确");
            }

            verificationCodeService.verifyCode("email", email, code);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("该邮箱未注册"));

            String token = JwtUtil.generateToken(user.getId(), user.getUsername());

            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;

            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setToken(token);
            response.setPoints(points);
            response.setBalance(balance);

            return CommonResponse.success(response);
        } catch (Exception e) {
            return CommonResponse.error(e.getMessage());
        }
    }
}