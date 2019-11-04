package cronapi.dateTime;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.i18n.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe que representa ...
 *
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-07
 *
 */

@CronapiMetaData(category = CategoryType.DATETIME, categoryTags = { "Data", "Data e hora", "Hora", "Date", "DateTime",
		"Time" })
public class Operations {

	private static Var getFromCalendar(Var date, int type) {
		return new Var(date.getObjectAsDateTime().get(type));
	}

	@CronapiMetaData(type = "function", name = "{{getSecondFromDate}}", nameTags = {
			"getSecond" }, description = "{{functionToGetSecondFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getSecond(Var value) {
		return getFromCalendar(value, Calendar.SECOND);
	}

	@CronapiMetaData(type = "function", name = "{{getMinuteFromDate}}", nameTags = {
			"getMinute" }, description = "{{functionToGetMinuteFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMinute(Var value) {
		return getFromCalendar(value, Calendar.MINUTE);
	}

	@CronapiMetaData(type = "function", name = "{{getHourFromDate}}", nameTags = {
			"getHour" }, description = "{{functionToGetHourFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getHour(Var value) {
		return getFromCalendar(value, Calendar.HOUR_OF_DAY);
	}

	@CronapiMetaData(type = "function", name = "{{getYearFromDate}}", nameTags = {
			"getYear" }, description = "{{functionToGetYearFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getYear(Var value) {
		return getFromCalendar(value, Calendar.YEAR);
	}

	@CronapiMetaData(type = "function", name = "{{getMonthFromDate}}", nameTags = {
			"getMonth" }, description = "{{functionToGetMonthFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMonth(Var value) {
		return new Var(getFromCalendar(value, Calendar.MONTH).getObjectAsInt()+1);
	}

	@CronapiMetaData(type = "function", name = "{{getDayFromDate}}", nameTags = {
			"getDay" }, description = "{{functionToGetDayFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getDay(Var value) {
		return getFromCalendar(value, Calendar.DAY_OF_MONTH);
	}

	@CronapiMetaData(type = "function", name = "{{getDayOfWeek}}", nameTags = {
			"getDayOfWeek" }, description = "{{functionToGetDayOfWeek}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getDayOfWeek(Var value) {
		return getFromCalendar(value, Calendar.DAY_OF_WEEK);
	}

	@CronapiMetaData(type = "function", name = "{{getLastDayFromMonth}}", nameTags = {
			"getLastDayFromMonth" }, description = "{{functionToGetLastDayFromMonth}}", params = { "{{month}}",
					"{{year}}" }, paramsType = { ObjectType.LONG, ObjectType.LONG }, returnType = ObjectType.LONG)
	public static final Var getLastDayFromMonth(Var month, Var year) {
		Calendar c = java.util.Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.MONTH, month.getObjectAsInt() - 1);
		c.set(Calendar.YEAR, year.getObjectAsInt());
		int actualMaximum = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		return new Var(actualMaximum);
	}

	@CronapiMetaData(type = "function", name = "{{newDate}}", nameTags = { "newDate",
			"createDate" }, description = "{{functionToNewDate}}", params = { "{{year}}", "{{month}}", "{{day}}",
					"{{hour}}", "{{minute}}", "{{second}}" }, paramsType = { ObjectType.LONG, ObjectType.LONG,
							ObjectType.LONG, ObjectType.LONG, ObjectType.LONG,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var newDate(Var year, Var month, Var day, Var hour, Var minute, Var second) {
		int y = year.getObjectAsInt();
		int m = month.getObjectAsInt() - 1;
		int d = day.getObjectAsInt();
		int h = hour.getObjectAsInt();
		int min = minute.getObjectAsInt();
		int s = second.getObjectAsInt();
		Calendar date = Calendar.getInstance();
		date.set(y, m, d, h, min, s);
		date.set(Calendar.MILLISECOND, 0);
		return new Var(date.getTime());
	}

	 @Deprecated
	  public static final Var updateDate(Var date, Var year, Var month, Var day, Var hour, Var minute, Var second, Var millisecond) {
		Calendar updatedDate = Calendar.getInstance();
		updatedDate.setTime(date.getObjectAsDateTime().getTime());
		int y = year.getObjectAsInt();
		int m = month.getObjectAsInt() - 1;
		int d = day.getObjectAsInt();
		int h = hour.getObjectAsInt();
		int min = minute.getObjectAsInt();
		int s = second.getObjectAsInt();
		int mili = millisecond.getObjectAsInt();
		updatedDate.set(y, m, d, h, min, s);
		updatedDate.set(Calendar.MILLISECOND, mili);
		return new Var(updatedDate.getTime());
	  }

	@CronapiMetaData(type = "function", name = "{{updateDate}}", nameTags = { "updateDate", "setDate" },
			description = "{{functionToUpdateDate}}", params = { "{{date}}", "{{type}}", "{{value}}" },
			paramsType = { ObjectType.DATETIME, ObjectType.STRING, ObjectType.LONG}, returnType = ObjectType.DATETIME)
	public static final Var updateNewDate(Var date, @ParamMetaData(type = ObjectType.STRING, description = "{{attribute}}", blockType = "util_dropdown",
			keys = {"year", "month", "day", "hour", "minute", "second", "millisecond"},
			values = {"{{year}}", "{{month}}", "{{day}}", "{{hour}}", "{{minute}}", "{{second}}", "{{millisecond}}"}) Var type, Var value) {
		Calendar updatedDate = Calendar.getInstance();
		updatedDate.setTime(date.getObjectAsDateTime().getTime());
		switch(type.getObjectAsString()){
			case "year":
				updatedDate.set(Calendar.YEAR, value.getObjectAsInt());
				break;
			case "month":
				updatedDate.set(Calendar.MONTH, value.getObjectAsInt() - 1);
				break;
			case "day":
				updatedDate.set(Calendar.DAY_OF_MONTH, value.getObjectAsInt());
				break;
			case "hour":
				updatedDate.set(Calendar.HOUR_OF_DAY, value.getObjectAsInt());
				break;
			case "minute":
				updatedDate.set(Calendar.MINUTE, value.getObjectAsInt());
				break;
			case "second":
				updatedDate.set(Calendar.SECOND, value.getObjectAsInt());
				break;
			case "millisecond":
				updatedDate.set(Calendar.MILLISECOND, value.getObjectAsInt());
				break;
		}
		return new Var(updatedDate.getTime());
	}

		@CronapiMetaData(type = "function", name = "{{getSecondsBetweenDates}}", nameTags = { "getSecondsBetweenDates",
			"getSecondsDiffDate", "diffDatesSeconds" }, description = "{{functionToGetSecondsBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getSecondsBetweenDates(Var dateVar, Var date2Var) {
		final long SECOND_IN_MILLIS = 1000;
		Date date = (dateVar.getObjectAsDateTime()).getTime();
		Date date2 = (date2Var.getObjectAsDateTime()).getTime();
		int resultBetween = (int) ((date.getTime() - date2.getTime()) / SECOND_IN_MILLIS);
		return new Var(resultBetween);
	}

	@CronapiMetaData(type = "function", name = "{{getMinutesBetweenDates}}", nameTags = { "getMinutesBetweenDates",
			"getMinutesDiffDate", "diffDatesMinutes" }, description = "{{functionToGetMinutesBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMinutesBetweenDates(Var dateVar, Var date2Var) {
		final long MINUTE_IN_MILLIS = 1000 * 60;
		Date date = (dateVar.getObjectAsDateTime()).getTime();
		Date date2 = (date2Var.getObjectAsDateTime()).getTime();
		int resultBetween = (int) ((date.getTime() - date2.getTime()) / MINUTE_IN_MILLIS);
		return new Var(resultBetween);
	}

	@CronapiMetaData(type = "function", name = "{{getHoursBetweenDates}}", nameTags = { "getHoursBetweenDates",
			"getHoursDiffDate", "diffDatesHours" }, description = "{{functionToGetHoursBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getHoursBetweenDates(Var dateVar, Var date2Var) {
		final long HOUR_IN_MILLIS = 1000 * 60 * 60;
		Date date = (dateVar.getObjectAsDateTime()).getTime();
		Date date2 = (date2Var.getObjectAsDateTime()).getTime();
		int resultBetween = (int) ((date.getTime() - date2.getTime()) / HOUR_IN_MILLIS);
		return new Var(resultBetween);
	}

	@CronapiMetaData(type = "function", name = "{{getDaysBetweenDates}}", nameTags = { "getDaysBetweenDates",
			"getDaysDiffDate", "diffDatesDays" }, description = "{{functionToGetDaysBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getDaysBetweenDates(Var dateVar, Var date2Var) {
		final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
		Date date = (dateVar.getObjectAsDateTime()).getTime();
		Date date2 = (date2Var.getObjectAsDateTime()).getTime();
		int daysBetween = (int) ((date.getTime() - date2.getTime()) / DAY_IN_MILLIS);
		return new Var(daysBetween);
	}

	@CronapiMetaData(type = "function", name = "{{getMonthsBetweenDates}}", nameTags = { "getMonthsBetweenDates",
			"getMonthsDiffDate", "diffDatesMonths" }, description = "{{functionToGetMonthsBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMonthsBetweenDates(Var dateVar, Var date2Var) {
		int yearBetween = 0, monthBetween = 0;
		Calendar date = Calendar.getInstance(), date2 = Calendar.getInstance();
		date.setTime((dateVar.getObjectAsDateTime()).getTime());
		date2.setTime((date2Var.getObjectAsDateTime()).getTime());
		yearBetween = (date.get(Calendar.YEAR) - date2.get(Calendar.YEAR)) * 12;
		monthBetween = date.get(Calendar.MONTH) - date2.get(Calendar.MONTH);
		monthBetween += yearBetween;
		if (date2.before(date) && date.get(Calendar.DAY_OF_MONTH) < date2.get(Calendar.DAY_OF_MONTH))
			monthBetween--;
		else if (date2.after(date) && date.get(Calendar.DAY_OF_MONTH) > date2.get(Calendar.DAY_OF_MONTH))
			monthBetween++;
		return new Var(monthBetween);
	}

	@CronapiMetaData(type = "function", name = "{{getYearsBetweenDates}}", nameTags = { "getYearsBetweenDates",
			"getYearsDiffDate", "diffDatesYears" }, description = "{{functionToGetYearsBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getYearsBetweenDates(Var dateVar, Var date2Var) {
		Calendar date = Calendar.getInstance(), date2 = Calendar.getInstance();
		date.setTime((dateVar.getObjectAsDateTime()).getTime());
		date2.setTime((date2Var.getObjectAsDateTime()).getTime());
			double diference = ((date.get(Calendar.YEAR) * 12 * 30) + (date.get(Calendar.MONTH) * 30)
					+ date.get(Calendar.DAY_OF_MONTH))
					- ((date2.get(Calendar.YEAR) * 12 * 30) + (date2.get(Calendar.MONTH) * 30)
							+ date2.get(Calendar.DAY_OF_MONTH));
			double result = diference / (12 * 30);

			if ((result - (int) result) >= 0.5) {
				return new Var((int) ++result);
			} else {
				return new Var((int) result);
			}
	}

  @CronapiMetaData(type = "function", name = "{{incSecond}}", nameTags = { "incSecond",
			"increaseSecond" }, description = "{{functionToIncSecond}}", params = { "{{date}}",
					"{{secondsToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incSeconds(Var value, Var second) {
		Calendar d = Calendar.getInstance();
		d.setTime((value.getObjectAsDateTime()).getTime());
		d.add(Calendar.SECOND , second.getObjectAsInt());
		return new Var(d.getTime());
	}

  @CronapiMetaData(type = "function", name = "{{incMinute}}", nameTags = { "incMinute",
			"increaseMinute" }, description = "{{functionToIncMinute}}", params = { "{{date}}",
					"{{minutesToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incMinute(Var value, Var minute) {
		Calendar d = Calendar.getInstance();
		d.setTime((value.getObjectAsDateTime()).getTime());
		d.add(Calendar.MINUTE , minute.getObjectAsInt());
		return new Var(d.getTime());
	}

  @CronapiMetaData(type = "function", name = "{{incHour}}", nameTags = { "incHour",
			"increaseHour" }, description = "{{functionToIncHour}}", params = { "{{date}}",
					"{{hoursToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incHour(Var value, Var hour) {
		Calendar d = Calendar.getInstance();
		d.setTime((value.getObjectAsDateTime()).getTime());
		d.add(Calendar.HOUR_OF_DAY , hour.getObjectAsInt());
		return new Var(d.getTime());
	}

	@CronapiMetaData(type = "function", name = "{{incDay}}", nameTags = { "incDay",
			"increaseDay" }, description = "{{functionToIncDay}}", params = { "{{date}}",
					"{{daysToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incDay(Var value, Var day) {
		Calendar d = Calendar.getInstance();
		d.setTime((value.getObjectAsDateTime()).getTime());
		d.add(Calendar.DAY_OF_MONTH, day.getObjectAsInt());
		return new Var(d.getTime());
	}

	@CronapiMetaData(type = "function", name = "{{incMonth}}", nameTags = { "incMonth",
			"increaseMonth" }, description = "{{functionToIncMonth}}", params = { "{{date}}",
					"{{monthsToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incMonth(Var value, Var month) {
		Calendar d = Calendar.getInstance();
		d.setTime( (value.getObjectAsDateTime()).getTime() );
		d.add(Calendar.MONTH, month.getObjectAsInt());
		return new Var(d.getTime());
	}

	@CronapiMetaData(type = "function", name = "{{incYear}}", nameTags = { "incYear",
			"increaseYear" }, description = "{{functionToIncYear}}", params = { "{{date}}",
					"{{yearsToIncrement}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incYear(Var value, Var year) {
		Calendar d = Calendar.getInstance();
		d.setTime( (value.getObjectAsDateTime()).getTime() );
		d.add(Calendar.YEAR, year.getObjectAsInt());
		return new Var(d.getTime());
	}

	@CronapiMetaData(type = "function", name = "{{getNow}}", nameTags = { "getNow", "now",
      "getDate" }, description = "{{functionToGetNow}}", returnType = ObjectType.DATETIME)
  public static final Var getNow() {
    Calendar cal = Calendar.getInstance();
    return new Var(cal.getTime());
  }

  @CronapiMetaData(type = "function", name = "{{getNowNoHour}}", nameTags = { "getNow", "now",
      "getDate" }, description = "{{functionToGetNowNoHour}}", returnType = ObjectType.DATETIME)
  public static final Var getNowNoHour() {
    Calendar cal = getNow().getObjectAsDateTime();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);

    return new Var(cal);
  }

    @CronapiMetaData(type = "function", name = "{{getNowInMilliseconds}}", nameTags = { "getNow", "now",
      "getDate","milliseconds" }, description = "{{getNowInMillisecondsDescription}}", returnType = ObjectType.LONG)
  public static final Var getNowInMilliseconds() {
    Calendar cal = Calendar.getInstance();
	  cal.setTime(new Date());
    return new Var(cal.getTimeInMillis());
  }

	@CronapiMetaData(type = "function", name = "{{formatDateTimeWithTimeZone}}", nameTags = {
			"format", "date", "datetime" }, description = "{{functionToFormatDateTime}}", params = { "{{date}}",
					"{{maskFormat}}", "{{timezoneFormat}}" }, paramsType = { ObjectType.DATETIME,
							ObjectType.STRING, ObjectType.LONG }, returnType = ObjectType.STRING)
	public static final Var formatDateTime(Var value, Var format, Var timeZone) {
	  if (format.isEmptyOrNull()) {
	    format = Var.valueOf(Messages.getString("DateTimeFormat"));
    }

	  if (format.getObjectAsString().equals("DateTimeFormat")) {
      format = Var.valueOf(Messages.getString("DateTimeFormat"));
    }

    if (format.getObjectAsString().equals("DateFormat")) {
      format = Var.valueOf(Messages.getString("DateFormat"));
    }

    if (format.getObjectAsString().equals("ISO8601")) {
      DateFormat frm = Utils.getISODateFormat();
      return Var.valueOf(frm.format(value.getObjectAsDateTime().getTime()));
    }

		SimpleDateFormat sdf = new SimpleDateFormat(format.getObjectAsString());
    sdf.setTimeZone(Utils.toTimeZone(timeZone));

		return Var.valueOf(sdf.format(value.getObjectAsDateTime().getTime()));
	}

  @CronapiMetaData(type = "function", name = "{{formatDateTime}}", nameTags = {
      "format", "date", "datetime" }, description = "{{functionToFormatDateTime}}", params = { "{{date}}",
      "{{maskFormat}}" }, paramsType = { ObjectType.DATETIME,
      ObjectType.STRING}, returnType = ObjectType.STRING)
  public static final Var formatDateTime(Var value, Var format) {
    return formatDateTime(value, format, Var.VAR_NULL);
  }
}
