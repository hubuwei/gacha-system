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
            
            // 获取钱包信息
            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;
            
            // 返回用户信息和钱包信息
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
            // 验证确认密码
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return CommonResponse.error(400, "两次输入的密码不一致");
            }
            
            User user = authService.register(
                request.getUsername(), 
                request.getPassword(),
                request.getPhone(),
                request.getEmail()
            );
            
            // 获取钱包信息
            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;
            
            // 返回用户信息和钱包信息
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
            
            // 参数验证
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
            
            // 验证邮箱验证码
            verificationCodeService.verifyCode("email", email, code);
            
            // 检查用户名是否已存在
            if (userRepository.findByUsername(username).isPresent()) {
                return CommonResponse.error(400, "用户名已存在");
            }
            
            // 检查邮箱是否已注册
            if (userRepository.findByEmail(email).isPresent()) {
                return CommonResponse.error(400, "该邮箱已被注册");
            }
            
            // 注册用户
            User user = authService.register(username, password, null, email);
            
            // 获取钱包信息
            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;
            
            // 返回用户信息
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
            
            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户 ID
            Long userId = JwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResponse.error(401, "Token 无效或已过期");
            }
            
            // 获取用户信息
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 获取钱包信息
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
     * 用户登出
     */
    @PostMapping("/logout")
    public CommonResponse<Void> logout() {
        // 由于 JWT 是无状态的，服务端只需返回成功，前端清除本地存储即可
        return CommonResponse.success(null);
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
            // 验证 Token
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
            
            // 验证文件
            if (file.isEmpty()) {
                return CommonResponse.error(400, "请选择要上传的文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return CommonResponse.error(400, "只能上传图片文件");
            }
            
            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return CommonResponse.error(400, "图片大小不能超过5MB");
            }
            
            // 创建上传目录
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());
            
            // 构建完整的访问URL（包含协议和主机）
            String scheme = request.getScheme(); // http or https
            String serverName = request.getServerName(); // localhost or IP
            int serverPort = request.getServerPort(); // 8084
            String contextPath = request.getContextPath(); // usually empty
            
            String baseUrl = scheme + "://" + serverName;
            if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
                baseUrl += ":" + serverPort;
            }
            
            String avatarUrl = baseUrl + contextPath + "/uploads/avatars/" + filename;
            
            // 更新数据库
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            
            // 返回结果
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
            // 验证手机号格式
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
            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return CommonResponse.error(400, "邮箱格式不正确");
            }
            
            // 验证用途参数
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
            // 验证 Token
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
            
            // 验证验证码
            verificationCodeService.verifyCode("sms", phone, code);
            
            // 更新用户手机号
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
            // 验证 Token
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
            
            // 验证验证码
            verificationCodeService.verifyCode("email", email, code);
            
            // 更新用户邮箱
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
            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return CommonResponse.error(400, "邮箱格式不正确");
            }
            
            // 验证验证码
            verificationCodeService.verifyCode("email", email, code);
            
            // 查找用户
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("该邮箱未注册"));
            
            // 生成 Token
            String token = JwtUtil.generateToken(user.getId(), user.getUsername());
            
            // 获取钱包信息
            Wallet wallet = walletRepository.findById(user.getId()).orElse(null);
            Integer points = wallet != null ? wallet.getPoints() : 0;
            Double balance = wallet != null ? wallet.getBalance() : 0.0;
            
            // 构建响应
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
