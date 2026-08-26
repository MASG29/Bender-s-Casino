package com.bendercasino.controller;

import com.bendercasino.dto.CreatePlayerRequest;
import com.bendercasino.dto.LoginRequest;
import com.bendercasino.dto.PlayerResponse;
import com.bendercasino.model.Player;
import com.bendercasino.service.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final PlayerService playerService;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse register(@Valid @RequestBody CreatePlayerRequest request) {
        Player player = playerService.create(request.name(), request.username(), request.firstName(),
                request.lastName(), request.email(), request.password());
        return toDto(player);
    }

    @PostMapping("/login")
    public PlayerResponse login(@Valid @RequestBody LoginRequest loginRequest,
                                HttpServletRequest httpRequest,
                                HttpServletResponse httpResponse) {
        Player player = playerService.login(loginRequest.identifier(), loginRequest.password());

        // Sessão HTTP do Spring Security: autentica e guarda o contexto na sessão.
        Authentication auth = new UsernamePasswordAuthenticationToken(
                player.getUsername(), null, AuthorityUtils.createAuthorityList("ROLE_PLAYER"));
        SecurityContextHolder.getContext().setAuthentication(auth);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), httpRequest, httpResponse);

        return toDto(player);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        new SecurityContextLogoutHandler().logout(request, response, null);
        return Map.of("status", "logged_out");
    }

    private PlayerResponse toDto(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getBalance(),
                new PlayerResponse.StatsDto(
                        player.getTotalWins(),
                        player.getTotalLosses(),
                        player.getTotalPushes(),
                        player.getTotalBlackjacks()
                )
        );
    }
}
