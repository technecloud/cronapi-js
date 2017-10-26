package cronapi;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class TokenUtils {
  
  private static String secret = "9SyECk96oDsTmXfogIieDI0cD/8FpnojlYSUJT5U9I/FGVmBz5oskmjOR8cbXTvoPjX+Pq/T/b1PqpHX0lYm0oCBjXWICA==";
  public static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
  
    public static String getUsernameFromToken(String token) {
      String username;
      try {
        Claims claims = getClaimsFromToken(token);
        username = claims.getSubject();
      }
      catch(Exception e) {
        username = null;
      }
      return username;
    }
    
    private static Claims getClaimsFromToken(String token) {
      Claims claims;
      try {
        claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
      }
      catch(Exception e) {
        claims = null;
      }
      return claims;
    }
}
