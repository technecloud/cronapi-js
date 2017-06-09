package cronapi;

import cronapi.i18n.Messages;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorResponse {
  private String error;
  private int status;
  private String stackTrace;

  public ErrorResponse(int status, Throwable ex) {
    this.error = getExceptionMessage(ex);
    this.status = status;

    if (ex != null) {
      StringWriter writer = new StringWriter();
      ex.printStackTrace(new PrintWriter(writer));

      this.stackTrace = writer.toString();
    }
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public String getExceptionMessage(Throwable ex) {

    String message = null;

    if (ex != null) {
      if (ex.getMessage() != null && !ex.getMessage().trim().isEmpty()) {
        message = ex.getMessage();
      } else {
        if (ex.getCause() != null) {
          return getExceptionMessage(ex.getCause());
        }
      }
    }

    if (message == null) {
      return Messages.getString("errorNotSpecified");
    }

    return message;

  }
}
