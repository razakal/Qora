
Lang = (function(){

var instance;

function constructor() { // normal singlton goes here
		var langObj = getResponseJson('translation.json');
		return { 
			translateByClass : function() {
				var x = document.getElementsByClassName("translate");
				var i;
				for (i = 0; i < x.length; i++) {
					x[i].innerHTML = Lang.getInstance().translate(x[i].innerHTML);
					
					if ('placeholder' in x[i])
					{
						x[i].placeholder = Lang.getInstance().translate(x[i].placeholder);
					}
				}
			},
			
			translateArray : function(array) {
				var newarray = [];
				for (i = 0; i < array.length; i++) {
					newarray[i] = Lang.getInstance().translate(array[i]);
				}
				return newarray;
			},
			
			translate : function(message) {
				//COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
				messageWithoutComment = message.replace("(?<!\\\\)#.*$", ""); 
				messageWithoutComment = messageWithoutComment.replace("\\#", "#");

				if (langObj == null) { 
					return messageWithoutComment;
				}
				
				if(!langObj.hasOwnProperty(message)) 
				{
					//IF NO SUITABLE TRANSLATION WITH THE COMMENT THEN RETURN WITHOUT COMMENT
					if(!langObj.hasOwnProperty(messageWithoutComment)) 
					{
						return messageWithoutComment;
					} 
					else 
					{
						return langObj[messageWithoutComment];
					}
				}
				
				res = langObj[message];

				if(!res)
				{
					return message;	
				}
				
				return res;
			}
		}
}

return {
	getInstance : function(){
		if(!instance) {  // check already exists
			instance = constructor();
		}
		return instance;
	}
}
})();