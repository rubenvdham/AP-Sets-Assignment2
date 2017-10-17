import java.io.PrintStream;
import java.util.HashMap;
import java.util.Scanner;
import java.math.BigInteger;
import java.util.regex.Pattern;

public class Main {
    static final String IDENTIFIER_FORMAT_EXCEPTION = "Space in Identifier not allowed",
            IDENTIFIER_BLANK_EXCEPTION = "An Identifier has to have a name",
            INVALID_STATEMENT = "Invalid statement, please read the documentation",
            IDENTIFIER_NOT_FOUND = ", parsed as identifier has no corresponding Set",
            DOUBLE_VALUE_SET = "Invalid Set: Set contains 2 elements with the same value",
            HELP_MESSAGE = "This Set interpreter works with operators +,-,* and Sets containing big Integers\n"
                    + "Set Interpreter REQUIRES you to omit spaces in identifiers\n"
                    +"However, you can use spaces and run the program with '--omit-spaces' to bypass this.\n\n"
                    +"Allowed statements:\n?<Set/Factor> to output a set or factor\n"
                    +"<Identifier>=<Set/Factor> to assign a Set to an Identifier.\n\n"
                    + "Set Interpreter by Kostas Moumtzakis & Ruben van der Ham";

    
    PrintStream out;
    HashMap<IdentifierInterface, SetInterface<BigInteger>> setCollection;

    Main(){
        out = new PrintStream(System.out);
        setCollection = new HashMap<IdentifierInterface, SetInterface<BigInteger>>();
    }
    
    public void parseStatement(Scanner input) throws APException {
    
    	if (nextCharIsLetter(input)) {
            parseAssignment(input);
    	} else if (nextCharIs(input, '?')) {
    		printStatement(input);
    	} else if (!nextCharIs(input, '/')) {
    		throw new APException(INVALID_STATEMENT);
    	}
    }
    
    public void parseAssignment(Scanner input) throws APException {
    	input.useDelimiter("\\=");
    	IdentifierInterface identifier = parseIdentifier(input.next());
    	//System.out.println("Identifier: " + identifier.toString());
    	
    	Scanner expression = new Scanner(input.next());
    	SetInterface<BigInteger> set = parseExpression(expression);
    	//System.out.println("Set: " + set.toString());
    	setCollection.put(identifier, set);
    }
    
    public void printStatement(Scanner input) throws APException {
		skipToken(input.next(), '?');
        SetInterface<BigInteger> set = parseExpression(input);
        System.out.println(SetToString(set));
    }

    private String SetToString(SetInterface set){
		StringBuilder output = new StringBuilder();
		if(set.goToFirstElement()){
			output.append(set.get());
			while(set.goToNextElement()){
				output.append(" ");
				output.append(set.get());
			}
		}
		return output.toString();
	}
    
    public IdentifierInterface parseIdentifier(String input) throws APException {
    	IdentifierInterface result = new Identifier();
    	
    	if(result.hasCorrectIdentifierFormat(input)) {
    		result.appendIdentifier(input);
        } else {
            //System.out.println("Wrong: " + input);
            throw new APException(IDENTIFIER_FORMAT_EXCEPTION);
        }
    	
        return result;
    }
    
    public SetInterface<BigInteger> parseExpression(Scanner expression) throws APException {
    	SetInterface<BigInteger> result = new Set<BigInteger>();
    	result = null;
    	StringBuilder term = new StringBuilder();
    	int complexFactors = 0;
    	
    	while (expression.hasNext()) {
    		
    		if (nextCharIs(expression, '(')) {
    			complexFactors += 1;
        		term.append(expression.next());
    			
    		} else if (nextCharIs(expression, ')')) {
    			complexFactors -= 1;
        		term.append(expression.next());
    			
    		} else if (nextCharIs(expression, '+') && complexFactors == 0) {
	    		skipToken(expression.next(), '+');
	    		
	    		if (result == null) {
		    		result = parseTerm(new Scanner(term.toString()));
	    		}
	    		term.setLength(0);
	    		
	    		if (nextCharIs(expression, '(')) {
	    			term.append(expression.nextLine());
	    			result = result.union(parseExpression(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		} else {
	    		
		    		while (expression.hasNext() && !(nextCharIs(expression, '+') || nextCharIs(expression, '-') || nextCharIs(expression, '|'))) {
	        		
		    			term.append(expression.next());
		    		}
		    		result = result.union(parseTerm(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		}
        		
        	} else if (nextCharIs(expression, '|') && complexFactors == 0) {
        		skipToken(expression.next(), '|');
	    		
	    		if (result == null) {
		    		result = parseTerm(new Scanner(term.toString()));
	    		}
	    		term.setLength(0);
	    		
	    		if (nextCharIs(expression, '(')) {
	    			term.append(expression.nextLine());
	    			result = result.symDifference(parseExpression(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		} else {
	    		
		    		while (expression.hasNext() && !(nextCharIs(expression, '+') || nextCharIs(expression, '-') || nextCharIs(expression, '|'))) {
	        		
		    			term.append(expression.next());
		    		}
		    		result = result.symDifference(parseTerm(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		}
        	} else if (nextCharIs(expression, '-') && complexFactors == 0) {
        		skipToken(expression.next(), '-');
	    		
	    		if (result == null) {
		    		result = parseTerm(new Scanner(term.toString()));
	    		}
	    		term.setLength(0);
	    		
	    		if (nextCharIs(expression, '(')) {
	    			term.append(expression.nextLine());
	    			result = result.complement(parseExpression(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		} else {
	    		
		    		while (expression.hasNext() && !(nextCharIs(expression, '+') || nextCharIs(expression, '-') || nextCharIs(expression, '|'))) {
	        		
		    			term.append(expression.next());
		    		}
		    		result = result.complement(parseTerm(new Scanner(term.toString())));
		    		//System.out.println("result: " + result.toString());
	    		}
        	} else {
        		term.append(expression.next());
        	}
    	}
    	if (complexFactors != 0) {
    		throw new APException("Missing parenthesis detected");
    	}
		//System.out.println("Term: " + term.toString());
    	if (result == null) {
    		result = parseTerm(new Scanner(term.toString()));
    	}
    	
    	return result;
    }
    
    public SetInterface<BigInteger> parseTerm(Scanner term) throws APException {
    	SetInterface<BigInteger> result = new Set<BigInteger>();
    	StringBuilder factor = new StringBuilder();
    	int complexFactors = 0;
    	
    	while (term.hasNext()) {
    		
    		if (nextCharIs(term, '(')) {
    			complexFactors += 1;
    			factor.append(term.next());
    			
    		} else if (nextCharIs(term, ')')) {
    			complexFactors -= 1;
    			factor.append(term.next());
    			
    		} else if (nextCharIs(term, '*') && complexFactors == 0) {
	    		skipToken(term.next(), '*');
        		result = parseFactor(new Scanner(factor.toString())).intersection(parseTerm(new Scanner(term.nextLine())));
        		return result;
        		
    		} else {
    			factor.append(term.next());
        	}
    	}
		//System.out.println("Factor: " + factor.toString());
		result = parseFactor(new Scanner(factor.toString()));
    	
    	return result;
    }
    
    public SetInterface<BigInteger> parseFactor(Scanner factor) throws APException {
    	SetInterface<BigInteger> result = new Set<BigInteger>();
    	int complexFactors = 0;
    	
    	while(factor.hasNext()) {
    		
	    	if (nextCharIsLetter(factor)) {
	    		StringBuilder id = new StringBuilder();
	    		
	    		id.append(factor.next());
	    		
	    		while (nextCharIsLetter(factor)) {
	        		id.append(factor.next());
	    		}
	    		IdentifierInterface identifier = parseIdentifier(id.toString());
	        	
    			if (setCollection.containsKey(identifier)) {
    				result = setCollection.get(identifier);
    			} else {
    				throw new APException("Identifier does not correspond to a Set");
    			}
	        	
	    	} else if (nextCharIs(factor, '{')) {
	    		skipToken(factor.next(), '{');
    			StringBuilder set = new StringBuilder();
    			
	    		while (!nextCharIs(factor, '}')) {
	    			set.append(factor.next());
	    		}
	    		skipToken(factor.next(), '}');
    			//System.out.println("testSet: " + set.toString());
	    		
	    		result = parseSet(set.toString());
	    		
	    	} else if (nextCharIs(factor, '(')) {
	    		skipToken(factor.next(), '(');
    			StringBuilder expression = new StringBuilder();
				complexFactors += 1;
				
	    		while (complexFactors != 0) {
	    				
    				while(!nextCharIs(factor, ')')) {
    					
    					if (nextCharIs(factor, '(')) {
    						complexFactors += 1;
    					}
    	    			expression.append(factor.next());
    				}
	    			if (nextCharIs(factor, ')')) {
	    				complexFactors -= 1;
	    			}
	    			if (complexFactors != 0 ) {
	    				 expression.append(factor.next());
	    			}
	    		}
	    		skipToken(factor.next(), ')');
    			//System.out.println("Expression2: " + expression.toString());
    			Scanner expressionScanner = new Scanner(expression.toString());
	    		
	    		result = parseExpression(expressionScanner);
	    	} else {
	    		throw new APException("What now...");
	    	}
    	}
    	
    	return result;
    }
    
    public SetInterface<BigInteger> parseSet(String numbers) {
    	SetInterface<BigInteger> result = new Set<BigInteger>();
    	Scanner parser = new Scanner(numbers);
    	parser.useDelimiter(",");
    	
    	while (parser.hasNext()) {
    		result.insert(parser.nextBigInteger());
    	}
    	parser.close();
		//System.out.println("TestSet2: " + result.toString());
    	
    	return result;
    }
    
    private void skipToken (String input, char c) throws APException {
    	Scanner token = new Scanner(input);
    	if (! nextCharIs(token, c)) {
    		throw new APException("Missing token: " + c);
	    }
    }
    
    private boolean nextCharIsLetter(Scanner input){
        input.useDelimiter("");
        return input.hasNext("[a-zA-Z]");
    }
    
    private boolean nextCharIs(Scanner input, char c){
        input.useDelimiter("");
        //System.out.println("Char: " + input.nextLine());
        return input.hasNext(Pattern.quote(c+""));
    }
    
    private void start() {
        Scanner in = new Scanner(System.in);
        Scanner statement;

        while(in.hasNextLine()) {
			statement = new Scanner(in.nextLine().replaceAll(" ", ""));

			try {
				parseStatement(statement);
			} catch (APException e) {
				System.out.println(e);
			}
		}
        in.close();
    }

    public static void main(String[] argv) {
            new Main().start();
    }
}
