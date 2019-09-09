/**
 * Copyright 2019 Pratanu Mandal
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 * 
 * 		The above copyright notice and this permission notice shall be included
 * 		in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package tk.pratanumandal.expr4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tk.pratanumandal.expr4j.common.MathExtras;
import tk.pratanumandal.expr4j.token.Function;
import tk.pratanumandal.expr4j.token.Operand;
import tk.pratanumandal.expr4j.token.Operator.Properties;
import tk.pratanumandal.expr4j.token.Operator.Properties.Associativity;

/**
 * The <code>OperatorRepository</code> class is the repository of all operators, functions, variables, and constants in the environment.
 * 
 * @author Pratanu Mandal
 *
 */
public class OperatorRepository {

	/**
	 * A map of all operators, functions, variables, and constants supported.
	 */
	public static final Map<String, Properties> OPERATORS;
	
	/**
	 * A list of all predefined operators, functions, variables, and constants.
	 */
	protected static final List<String> PREDEFINED_OPERATORS;
	
	
	static {
		
		OPERATORS = new HashMap<>();
		
		// priority is in increasing order from 0 to infinity
		
		// parenthesis - 0
		OPERATORS.put("(", new Properties(1, 0));
		OPERATORS.put(")", new Properties(1, 0));
		
		// no argument - 0
		OPERATORS.put(",", new Properties(0, 0));
		
		// binary - 1
		OPERATORS.put("+", new Properties(2, 1));
		OPERATORS.put("-", new Properties(2, 1));
		
		// binary - 2
		OPERATORS.put("*", new Properties(2, 2));
		OPERATORS.put("/", new Properties(2, 2));
		OPERATORS.put("%", new Properties(2, 2));
		
		// binary - 3
		OPERATORS.put("^", new Properties(2, 3));
		
		// unary - 4
		OPERATORS.put("uminus", new Properties(1, 4, Associativity.NO));
		OPERATORS.put("uplus", new Properties(1, 4, Associativity.NO));
		
		// functions - 4
		OPERATORS.put("abs", new Properties(1, 4, (operands) -> {return new Operand(Math.abs(operands[0].toDouble()));}));
		
		OPERATORS.put("sin", new Properties(1, 4, (operands) -> {return new Operand(Math.sin(operands[0].toDouble()));}));
		OPERATORS.put("cos", new Properties(1, 4, (operands) -> {return new Operand(Math.cos(operands[0].toDouble()));}));
		OPERATORS.put("tan", new Properties(1, 4, (operands) -> {return new Operand(Math.tan(operands[0].toDouble()));}));
		
		OPERATORS.put("asin", new Properties(1, 4, (operands) -> {return new Operand(Math.asin(operands[0].toDouble()));}));
		OPERATORS.put("acos", new Properties(1, 4, (operands) -> {return new Operand(Math.acos(operands[0].toDouble()));}));
		OPERATORS.put("atan", new Properties(1, 4, (operands) -> {return new Operand(Math.atan(operands[0].toDouble()));}));
		
		OPERATORS.put("sinh", new Properties(1, 4, (operands) -> {return new Operand(Math.sinh(operands[0].toDouble()));}));
		OPERATORS.put("cosh", new Properties(1, 4, (operands) -> {return new Operand(Math.cosh(operands[0].toDouble()));}));
		OPERATORS.put("tanh", new Properties(1, 4, (operands) -> {return new Operand(Math.tanh(operands[0].toDouble()));}));
		
		OPERATORS.put("asinh", new Properties(1, 4, (operands) -> {return new Operand(MathExtras.asinh(operands[0].toDouble()));}));
		OPERATORS.put("acosh", new Properties(1, 4, (operands) -> {return new Operand(MathExtras.acosh(operands[0].toDouble()));}));
		OPERATORS.put("atanh", new Properties(1, 4, (operands) -> {return new Operand(MathExtras.atanh(operands[0].toDouble()));}));
		
		OPERATORS.put("deg", new Properties(1, 4, (operands) -> {return new Operand(Math.toDegrees(operands[0].toDouble()));}));
		OPERATORS.put("rad", new Properties(1, 4, (operands) -> {return new Operand(Math.toRadians(operands[0].toDouble()));}));
		
		OPERATORS.put("round", new Properties(1, 4, (operands) -> {return new Operand(Math.round(operands[0].toDouble()));}));
		OPERATORS.put("floor", new Properties(1, 4, (operands) -> {return new Operand(Math.floor(operands[0].toDouble()));}));
		OPERATORS.put("ceil", new Properties(1, 4, (operands) -> {return new Operand(Math.ceil(operands[0].toDouble()));}));
		
		OPERATORS.put("ln", new Properties(1, 4, (operands) -> {return new Operand(Math.log(operands[0].toDouble()));}));
		OPERATORS.put("log10", new Properties(1, 4, (operands) -> {return new Operand(Math.log10(operands[0].toDouble()));}));
		OPERATORS.put("log", new Properties(2, 4, (operands) -> {return new Operand(MathExtras.log(operands[0].toDouble(), operands[1].toDouble()));}));
		
		OPERATORS.put("sqrt", new Properties(1, 4, (operands) -> {return new Operand(Math.sqrt(operands[0].toDouble()));}));
		OPERATORS.put("cbrt", new Properties(1, 4, (operands) -> {return new Operand(Math.cbrt(operands[0].toDouble()));}));
		
		// zero argument - 5
		OPERATORS.put("rand", new Properties(0, 5, Associativity.NO));
		OPERATORS.put("pi", new Properties(0, 5, Associativity.NO));
		OPERATORS.put("e", new Properties(0, 5, Associativity.NO));
		
		
		
		// create list of predefined operators
		
		Set<String> opSet = OPERATORS.keySet();
		
		PREDEFINED_OPERATORS = new ArrayList<>(opSet.size());
		
	    for (String op : opSet) {
	    	PREDEFINED_OPERATORS.add(op);
	    }
	    
	}
	
	/**
	 * Utility method to determine if a string is a supported operator.
	 * 
	 * @param op The string to check
	 * @return true if the string is a supported operator, false otherwise
	 */
	public static boolean isOperator(String op) {
		return OPERATORS.containsKey(op);
	}
	
	/**
	 * Utility method to determine if a string is a supported function.
	 * 
	 * @param op The string to check
	 * @return true if the string is a supported function, false otherwise
	 */
	public static boolean isFunction(String op) {
		return OPERATORS.get(op).function != null;
	}
	
	/**
	 * Utility method to add user defined function to the environment.<br>
	 * Method names can consist of lowercase letters, uppercase letters, and digits.
	 * 
	 * @param fName Function name - e.g. for function name 'myFunc', it will be called as myFunc(...)
	 * @param params Number of parameters
	 * @param function Function definition
	 */
	public static void addFunction(String fName, int params, Function function) {
		if (fName == null) throw new NullPointerException("Function name cannot be null.");
		if (!fName.matches("[a-zA-Z0-9]+")) throw new RuntimeException("Not a valid function name: " + fName + ".");
		if (function == null) throw new NullPointerException("Function cannot be null.");
		if (PREDEFINED_OPERATORS.contains(fName)) throw new RuntimeException("Cannot override predefined function: " + fName + ".");
		OPERATORS.put(fName, new Properties(params, 4, function));
	}
	
	/**
	 * Utility method to remove user defined function from the environment.<br>
	 * Method names can consist of lowercase letters, uppercase letters, and digits.
	 * 
	 * @param fName Function name - e.g. for function name 'myFunc', it will be called as myFunc(...)
	 */
	public static void removeFunction(String fName) {
		if (fName == null) throw new NullPointerException("Function name cannot be null.");
		if (!fName.matches("[a-zA-Z0-9]+")) throw new RuntimeException("Not a valid function name: " + fName + ".");
		if (!isOperator(fName) || !isFunction(fName)) throw new RuntimeException("Function not found: " + fName + ".");
		if (PREDEFINED_OPERATORS.contains(fName)) throw new RuntimeException("Cannot remove predefined function: " + fName + ".");
		OPERATORS.remove(fName);
	}
	
}
