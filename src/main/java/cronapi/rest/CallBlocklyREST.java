package cronapi.rest;

import org.springframework.web.bind.annotation.*;

import cronapi.Var;

import java.util.List;

@RestController
@RequestMapping(value = "/api/cronapi/call")
public class CallBlocklyREST {

	@RequestMapping(method = RequestMethod.GET, value = "/{class}")
	public Var getNoParam(@PathVariable("class") String clazz) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}")
	public Var getOneParam(@PathVariable("class") String clazz, @PathVariable("param1") String param1)
			throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}")
	public Var getTwoParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
							@PathVariable("param2") String param2) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2));
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{class}/{param1}/{param2}/{param3}")
	public Var getThreeParams(@PathVariable("class") String clazz, @PathVariable("param1") String param1,
							  @PathVariable("param2") String param2, @PathVariable("param3") String param3) throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), new Var(param1), new Var(param2), new Var(param3));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{class}")
	public Var postParams(@RequestBody Var[] vars, @PathVariable("class") String clazz)
			throws Exception {
		return cronapi.util.Operations.callBlockly(new Var(clazz), vars);
	}

}