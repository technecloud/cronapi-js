package cronapi;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenUtils {

  private static String secret;
  public static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";

  static {
    secret = AppConfig.token();
  }

  public static String getUsernameFromToken(String token) {
    String username;
    try {
      Claims claims = getClaimsFromToken(token);
      username = claims.getSubject();
    } catch (Exception e) {
      username = null;
    }
    return username;
  }

  public static Claims getClaimsFromToken(String token) {
    Claims claims;
    try {
      claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    } catch (Exception e) {
      claims = null;
    }
    return claims;
  }

  public static String addClaimToToken(String token, String key, Object value) {
    try {
      Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
      claims.put(key, value);
      return Jwts.builder().setClaims(claims).setExpiration(claims.getExpiration())
          .signWith(SignatureAlgorithm.HS512, secret).compact();
    } catch (Exception e) {
      throw new RuntimeException("Token is not in the header");
    }
  }
}
