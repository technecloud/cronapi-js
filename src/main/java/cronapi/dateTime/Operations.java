package cronapi.dateTime;

import java.util.Calendar;
import java.util.Date;

import cronapi.CronapiMetaData;
import cronapi.Utils;
import cronapi.Var;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;

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
		return new Var(Utils.getFromCalendar((Date) date.getObject(), type));
	}

	@CronapiMetaData(type = "function", name = "{{getSecondFromDate}}", nameTags = {
			"getSecond" }, description = "{{functionToGetSecondFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getSecond(Var value) throws Exception {
		return getFromCalendar(value, Calendar.SECOND);
	}

	@CronapiMetaData(type = "function", name = "{{getMinuteFromDate}}", nameTags = {
			"getMinute" }, description = "{{functionToGetMinuteFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMinute(Var value) throws Exception {
		return getFromCalendar(value, Calendar.MINUTE);
	}

	@CronapiMetaData(type = "function", name = "{{getHourFromDate}}", nameTags = {
			"getHour" }, description = "{{functionToGetHourFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getHour(Var value) throws Exception {
		return getFromCalendar(value, Calendar.HOUR_OF_DAY);
	}

	@CronapiMetaData(type = "function", name = "{{getYearFromDate}}", nameTags = {
			"getYear" }, description = "{{functionToGetYearFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getYear(Var value) throws Exception {
		return getFromCalendar(value, Calendar.YEAR);
	}

	@CronapiMetaData(type = "function", name = "{{getMonthFromDate}}", nameTags = {
			"getMonth" }, description = "{{functionToGetMonthFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMonth(Var value) throws Exception {
		return getFromCalendar(value, Calendar.MONTH);
	}

	@CronapiMetaData(type = "function", name = "{{getDayFromDate}}", nameTags = {
			"getDay" }, description = "{{functionToGetDayFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getDay(Var value) throws Exception {
		return getFromCalendar(value, Calendar.DAY_OF_MONTH);
	}

	@CronapiMetaData(type = "function", name = "{{getMonthsBetweenDates}}", nameTags = { "getMonthsBetweenDates",
			"getMonthsDiffDate", "diffDatesMonths" }, description = "{{functionToGetMonthsBetweenDates}}", params = {
					"{{largerDateToBeSubtracted}}", "{{smallerDateToBeSubtracted}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.DATETIME }, returnType = ObjectType.LONG)
	public static final Var getMonthsBetweenDates(Var dateVar, Var date2Var) throws Exception {
		int yearBetween = 0, monthBetween = 0;
		Calendar date = Calendar.getInstance(), date2 = Calendar.getInstance();
		date.setTime((Date) dateVar.getObject());
		date2.setTime((Date) date2Var.getObject());
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
	public static final Var getYearsBetweenDates(Var dateVar, Var date2Var) throws Exception {
		int yearBetween = 0;
		Calendar date = Calendar.getInstance(), date2 = Calendar.getInstance();
		date.setTime((Date) dateVar.getObject());
		date2.setTime((Date) date2Var.getObject());
		yearBetween = (date.get(Calendar.YEAR) - date2.get(Calendar.YEAR));
		if (date2.before(date) && (date.get(Calendar.MONTH) < date2.get(Calendar.MONTH)
				|| date.get(Calendar.DAY_OF_MONTH) < date2.get(Calendar.DAY_OF_MONTH)))
			yearBetween--;
		else if (date2.after(date) && (date.get(Calendar.MONTH) > date2.get(Calendar.MONTH)
				|| date.get(Calendar.DAY_OF_MONTH) > date2.get(Calendar.DAY_OF_MONTH)))
			yearBetween++;
		return new Var(yearBetween);
	}
	
	@CronapiMetaData(type = "function", name = "{{incDay}}", nameTags = { "incDay",
			"increaseDay" }, description = "{{functionToIncDay}}", params = {
					"{{date}}", "{{daysToIncrement}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incDay(Var value, Var day) throws Exception {
    Calendar d = Calendar.getInstance();
    d.setTime((Date)value.getObject());
    d.add(Calendar.DAY_OF_MONTH, day.getObjectAsInt());
    return new Var(d.getTime());
  }	
  
	@CronapiMetaData(type = "function", name = "{{incMonth}}", nameTags = { "incMonth",
			"increaseMonth" }, description = "{{functionToIncMonth}}", params = {
					"{{date}}", "{{monthsToIncrement}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incMonth(Var value, Var month) throws Exception {
    Calendar d = Calendar.getInstance();
    d.setTime((Date)value.getObject());
    d.add(Calendar.MONTH, month.getObjectAsInt());
    return new Var(d.getTime());
  }	
  
  @CronapiMetaData(type = "function", name = "{{incYear}}", nameTags = { "incYear",
			"increaseYear" }, description = "{{functionToIncYear}}", params = {
					"{{date}}", "{{yearsToIncrement}}" }, paramsType = {
							ObjectType.DATETIME, ObjectType.LONG }, returnType = ObjectType.DATETIME)
	public static final Var incYear(Var value, Var year) throws Exception {
    Calendar d = Calendar.getInstance();
    d.setTime((Date)value.getObject());
    d.add(Calendar.YEAR, year.getObjectAsInt());
    return new Var(d.getTime());
  }	
}
