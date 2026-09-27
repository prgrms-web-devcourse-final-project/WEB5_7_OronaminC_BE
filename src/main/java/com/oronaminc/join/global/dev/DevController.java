//package com.oronaminc.join.global.dev;
//
//import com.oronaminc.join.member.dao.MemberRepository;
//import com.oronaminc.join.member.domain.Member;
//import com.oronaminc.join.member.domain.MemberType;
//import com.oronaminc.join.member.security.MemberDetails;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
/// /@Profile("local")
//@RestController
//@Tag(name = "개발용 API")
//@RequestMapping("/dev")
//@RequiredArgsConstructor
//public class DevController {
//    private final MemberRepository memberRepository;
//
//    @PostMapping("/join")
//    @ResponseStatus(HttpStatus.OK)
//    public Member devJoin(@RequestBody DevJoinRequest devJoinRequest) {
//        return memberRepository.save(
//                Member.builder()
//                        .email(devJoinRequest.email())
//                        .nickname(devJoinRequest.nickname())
//                        .memberType(MemberType.MEMBER)
//                        .build()
//        );
//    }
//
//    @PostMapping("/login")
//    @ResponseStatus(HttpStatus.OK)
//    public void devLogin(@RequestBody DevLoginRequest devLoginRequest, HttpServletRequest request) {
//        Member member = memberRepository.findById(devLoginRequest.memberId())
//                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다."));
//
//        MemberDetails memberDetails = MemberDetails.builder()
//                .id(member.getId())
//                .name(member.getEmail())
//                .nickname(member.getNickname())
//                .role(member.getMemberType())
//                .build();
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(
//                memberDetails,
//                null,
//                List.of(new SimpleGrantedAuthority(memberDetails.getRole()))
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        HttpSession session = request.getSession(true);
//        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
//    }
//
//
//}
