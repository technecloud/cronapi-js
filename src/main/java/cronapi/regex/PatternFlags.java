package cronapi.regex;

import java.util.regex.Pattern;

/**
 * Enumeração que representa ...
 *
 * @author Samuel Almeida
 * @version 1.0
 * @since 2020-04-14
 *
 */

public enum PatternFlags {

    CASE_INSENSITIVE{ @Override  public int getValue(){  return Pattern.CASE_INSENSITIVE; } },
    MULTILINE{ @Override  public int getValue(){  return Pattern.MULTILINE; } },
    DOTALL{ @Override  public int getValue(){  return Pattern.DOTALL; } },
    UNICODE_CASE{ @Override  public int getValue(){  return Pattern.UNICODE_CASE; } },
    CANON_EQ{ @Override  public int getValue(){  return Pattern.CANON_EQ; } },
    UNIX_LINES{ @Override  public int getValue(){  return Pattern.UNIX_LINES; } },
    LITERAL{ @Override  public int getValue(){  return Pattern.LITERAL; } },
    UNICODE_CHARACTER_CLASS{ @Override  public int getValue(){  return Pattern.UNICODE_CHARACTER_CLASS; } },
    COMMENTS{ @Override  public int getValue(){  return Pattern.COMMENTS; } };


    public abstract int getValue();

}
