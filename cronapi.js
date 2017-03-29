(function() {
	'use strict';

  window.cronapi = {
  	/**
  	 * @category Conversion
  	 * categorySynonymous Conversão|Convert
  	 */
  	conversion : {
  		/**
    	 * @type function
    	 * @name Texto para texto binário
    	 * @nameSynonymous asciiToBinary
    	 * @description Função para converter texto para texto binário
    	 * @param {string} astring - The x value.
    	 */
  		asciiToBinary : function(astring) {
  			var binary = "";
  			if (astring.length > 0) {
  				for (var i = 0; i < astring.length; i++) {
  					var value = astring.charCodeAt(i);
  					for (var j = 7; j >= 0; j--) {
  						binary += ((value >> j) & 1);
  					}
  				}
  			}
  			return binary;
  		}
  	}
  };

})();


