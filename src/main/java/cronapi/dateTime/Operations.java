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
	protected static final Var getSecond(Var value) throws Exception {
		return getFromCalendar(value, Calendar.SECOND);
	}
  
	@CronapiMetaData(type = "function", name = "{{getMinuteFromDate}}", nameTags = {
			"getMinute" }, description = "{{functionToGetMinuteFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	protected static final Var getMinute(Var value) throws Exception {
		return getFromCalendar(value, Calendar.MINUTE);
	}
	
	@CronapiMetaData(type = "function", name = "{{getHourFromDate}}", nameTags = {
			"getHour" }, description = "{{functionToGetHourFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	protected static final Var getHour(Var value) throws Exception {
		return getFromCalendar(value, Calendar.HOUR_OF_DAY);
	}
	
	@CronapiMetaData(type = "function", name = "{{getYearFromDate}}", nameTags = {
			"getYear" }, description = "{{functionToGetYearFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	protected static final Var getYear(Var value) throws Exception {
		return getFromCalendar(value, Calendar.YEAR);
	}
	
	@CronapiMetaData(type = "function", name = "{{getMonthFromDate}}", nameTags = {
			"getMonth" }, description = "{{functionToGetMonthFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	protected static final Var getMonth(Var value) throws Exception {
		return getFromCalendar(value, Calendar.MONTH);
	}
	
	@CronapiMetaData(type = "function", name = "{{getDayFromDate}}", nameTags = {
			"getDay" }, description = "{{functionToGetDayFromDate}}", params = {
					"{{date}}" }, paramsType = { ObjectType.DATETIME }, returnType = ObjectType.LONG)
	protected static final Var getDay(Var value) throws Exception {
		return getFromCalendar(value, Calendar.DAY_OF_MONTH);
	}

}
