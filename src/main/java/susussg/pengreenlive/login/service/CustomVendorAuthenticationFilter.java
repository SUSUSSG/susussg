package susussg.pengreenlive.login.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import susussg.pengreenlive.login.dto.Member;
import susussg.pengreenlive.vendor.service.VendorService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CustomVendorAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private VendorService vendorService;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {


        try {
            log.info("요청 메서드 {}", request.getMethod());

            if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
                log.info("OPTIONS 요청 무시");
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                return null;
            }

            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");
            log.info("로그인 정보 {} {}", username, password );

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            return getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        Map<String, String> result = new HashMap<>();

        Member member = (Member) authResult.getPrincipal();

        Long channelSeq = vendorService.getChannelSeqByVendorSeq(member.getVendorSeq());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", member.getUsername());
        userInfo.put("name", member.getUserNm());
        userInfo.put("vendorSeq", member.getVendorSeq());
        userInfo.put("role", member.getAuthorities().toString());
        userInfo.put("channelSeq", channelSeq);

        // SecurityContext에 Member 객체 저장
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        log.info("생성된 세션 아이디 {}", request.getSession().getId());

        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        log.info("session info {}", SecurityContextHolder.getContext().getAuthentication());

        result.put("user", objectMapper.writeValueAsString(userInfo));
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Content-Length");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, String> result = new HashMap<>();
        result.put("message", "Authentication Failed");
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
