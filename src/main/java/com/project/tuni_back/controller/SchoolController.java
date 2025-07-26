package com.project.tuni_back.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.tuni_back.bean.vo.UniversityVO;
import com.project.tuni_back.service.AuthService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SchoolController {

    private final AuthService authService;

    /**
     * 대학교 선택 및 이메일 입력 폼을 보여주는 페이지
     */
    @GetMapping("/school/verify")
    public String showVerifyForm(Model model) {
        List<UniversityVO> universities = authService.getAllUniversities();
        model.addAttribute("universities", universities);
        return "school-verify"; // school-verify.html 템플릿을 반환
    }

    /**
     * 폼 제출을 처리하여 이메일을 검증하고 코드를 발송
     */
    @PostMapping("/school/verify")
    public String processVerifyForm(@RequestParam Long universityId,
                                    @RequestParam String email,
                                    RedirectAttributes redirectAttributes) {
        try {
            authService.validateAndSendCode(universityId, email);
            // 성공 시, 코드 입력 페이지로 리다이렉트 (이메일 정보 전달)
            redirectAttributes.addAttribute("email", email);
            return "redirect:/verify-code-form"; // 코드 입력 폼 페이지 (추후 구현)
        } catch (IllegalArgumentException e) {
            // 실패 시, 에러 메시지와 함께 원래 폼으로 리다이렉트
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/school/verify";
        }
    }
    
    /**
     * 인증 코드를 입력하는 폼 페이지를 보여주는 메소드
     */
    @GetMapping("/verify-code-form")
    public String showCodeForm(@RequestParam String email, Model model) {
        // 이메일을 모델에 담아 페이지로 전달
        model.addAttribute("email", email);
        return "verify-code-form"; // verify-code-form.html 템플릿 반환
    }
    
    @GetMapping("/register-form")
    public String showRegisterForm(@RequestParam String email, @RequestParam String code, Model model) {
        // 이전 페이지에서 검증된 이메일과 코드를 모델에 담아 전달
        model.addAttribute("email", email);
        model.addAttribute("code", code);
        return "nickname-form"; // nickname-form.html 템플릿 반환
    }
}