package cronapi.report.odata;

import com.stimulsoft.lib.utils.StiStringUtil;

import static org.springframework.util.StringUtils.trimLeadingCharacter;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

final class StiUrl {

  static String combine(String[] uriParts) {
    if (uriParts == null || uriParts.length == 0) {
      return StiStringUtil.EMPTY;
    }

    char[] trims = new char[]{'\\', '/'};

    if (uriParts[0] == null) {
      uriParts[0] = StiStringUtil.EMPTY;
    }

    String uri = trimTrailingCharacter(uriParts[0], trims[1]);

    for (int i = 1; i < uriParts.length; i++) {
      String currentPart = StiStringUtil.EMPTY;

      if (uriParts[i] != null) {
        currentPart = trimLeadingCharacter(uriParts[i], trims[0]);
        currentPart = trimLeadingCharacter(currentPart, trims[1]);
      }

      uri = trimTrailingCharacter(uri, trims[0]);
      uri = trimTrailingCharacter(uri, trims[1]);
      uri = String.format("%s/%s", uri, currentPart);
    }

    return uri;
  }
}