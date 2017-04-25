package cronapi.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cronapi.Var;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-04-25
 *
 */
@RestController
@RequestMapping(value = "/api/cronapi/call")
public class CallBlocklyREST {

	/**
	 * Construtor
	 **/
	public CallBlocklyREST() {
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}")
	public Object getNoParam(@PathVariable("class") String clazz) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz)).getObject();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}")
	public Object getOneParam(@PathVariable("class") String clazz, @PathVariable("param1") String param1)
			throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1)).getObject();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}")
	public Object getTwoParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
			@PathVariable("param2") String param2) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2)).getObject();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}/{param3}")
	public Object getThreeParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
			@PathVariable("param2") String param2, @PathVariable("param3") String param3) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2), new Var(param3))
				.getObject();
	}

	/**
	 * 
	 * {
	 * "params": [
	 *              12
	 *            ]
	 *  } 
	 *
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/{class}")
	public Object postParams(@RequestBody Map<String, Object> body, @PathVariable("class") String clazz)
			throws Exception {

		ArrayList<?> paramList = (ArrayList<?>) body.get("params");

		Var[] vars = new Var[paramList.size()];
		int i = 0;
		for (Object value : paramList) {
			vars[i++] = new Var(value);
		}
		Var result = cronapi.util.Operations.callBlockly(new Var(clazz), vars);

		return result.getObject();
	}

}
