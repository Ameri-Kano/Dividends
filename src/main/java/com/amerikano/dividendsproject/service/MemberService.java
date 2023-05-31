package com.amerikano.dividendsproject.service;

import com.amerikano.dividendsproject.exception.impl.AlreadyExistUserException;
import com.amerikano.dividendsproject.exception.impl.PasswordNotMatchException;
import com.amerikano.dividendsproject.exception.impl.UserNotExistException;
import com.amerikano.dividendsproject.model.Auth;
import com.amerikano.dividendsproject.persist.MemberRepository;
import com.amerikano.dividendsproject.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Couldn't find user -> " + username));
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(passwordEncoder.encode(member.getPassword()));
        MemberEntity result = memberRepository.save(member.toEntity());

        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new UserNotExistException());

        if (!passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new PasswordNotMatchException();
        }

        return user;
    }
}
