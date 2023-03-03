/**
 * Copyright 2023 Pratanu Mandal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package in.pratanumandal.expr4j;

import in.pratanumandal.expr4j.exception.Expr4jException;
import in.pratanumandal.expr4j.token.*;
import in.pratanumandal.expr4j.token.Operator.OperatorType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The <code>Expression&lt;T&gt;</code> class represents a parsed expression that can be evaluated.
 * 
 * @author Pratanu Mandal
 * @since 1.0
 *
 * @param <T> The type of operand for this expression
 */
public class Expression<T> {
	
	/**
	 * The <code>Node</code> class represents a node of the expression tree.<br><br>
	 * 
	 * @author Pratanu Mandal
	 *
	 */
	public static class Node {
		/**
		 * Children of this node.
		 */
		public final List<Node> children;
		
		/**
		 * Token contained in this node.<br>
		 * A token can be an operand, operator, function, variable, or constant.
		 */
		public final Token token;

		/**
		 * Parameterized constructor.
		 * 
		 * @param token The token in this node
		 */
		public Node(Token token) {
			this.token = token;
			if (token instanceof Function || token instanceof Operator) {
				this.children = new ArrayList<>();
			}
			else {
				this.children = null;
			}
		}
	}
	
	/**
	 * Root node of the expression tree.
	 */
	public Node root;
	
	/**
	 * Map to hold the constants.
	 */
	private final Map<String, T> constants;
	
	private final OperandRepresentation<T> operandRepresentation;
	
	/**
	 * Parameterized constructor.
	 * 
	 * @param constants Map of constants
	 * @param operandRepresentation Instance of <code>OperandRepresentation</code>
	 */
	public Expression(Map<String, T> constants, OperandRepresentation<T> operandRepresentation) {
		this.constants = new HashMap<>(constants);
		this.operandRepresentation = operandRepresentation;
	}

	/**
	 * Get unmodifiable map of constants for this expression.
	 * 
	 * @return Map of constants
	 */
	public Map<String, T> getConstants() {
		return Collections.unmodifiableMap(constants);
	}
	
	/**
	 * Recursively evaluate the expression tree and return the result.
	 * 
	 * @param node Current node of the expression tree
	 * @param variables Map of variables
	 * @return Result of expression evaluation
	 */
	@SuppressWarnings("unchecked")
	protected Operand<T> evaluate(Node node, Map<String, T> variables) {
		// encountered variable
		if (node.token instanceof Variable) {
			Variable variable = (Variable) node.token;
			
			if (!variables.containsKey(variable.label)) {
				throw new Expr4jException("Variable not found: " + variable.label);
			}
			
			return new Operand<T>(variables.get(variable.label));
		}
		
		// encountered function
		else if (node.token instanceof Function) {
			Function<T> function = (Function<T>) node.token;
			
			int operandCount = function.parameters;
			if (node.children.size() != operandCount) {
				throw new Expr4jException("Invalid expression");
			}
			
			List<T> operands = new ArrayList<>();
			for (int i = 0; i < operandCount; i++) {
				operands.add(evaluate(node.children.get(i), variables).value);
			}
			
			return new Operand<T>(function.evaluate(operands));
		}
		
		// encountered operator
		else if (node.token instanceof Operator) {
			Operator<T> operator = (Operator<T>) node.token;
			
			int operandCount = (operator.operatorType == OperatorType.INFIX || operator.operatorType == OperatorType.INFIX_RTL) ? 2 : 1;
			if (node.children.size() != operandCount) {
				throw new Expr4jException("Invalid expression");
			}
			
			List<T> operands = new ArrayList<>();
			for (int i = 0; i < operandCount; i++) {
				operands.add(evaluate(node.children.get(i), variables).value);
			}
			
			return new Operand<T>(operator.evaluate(operands));
		}
		
		// encountered operand
		else {
			return (Operand<T>) node.token;
		}
	}
	
	/**
	 * Evaluate the expression against a set of variables.<br>
	 * Variables passed to this method override an predefined constants with the same label.
	 * 
	 * @param variables Map of variables
	 * @return Evaluated result
	 */
	public T evaluate(Map<String, T> variables) {
		if (root == null) {
			throw new Expr4jException("Invalid expression");
		}
		
		Map<String, T> constantsAndVariables = new HashMap<>(constants);
		if (variables != null) constantsAndVariables.putAll(variables);
		
		return evaluate(root, constantsAndVariables).value;
	}
	
	/**
	 * Evaluate the expression.
	 * 
	 * @return Evaluated result
	 */
	public T evaluate() {
		return evaluate(new HashMap<String, T>());
	}
	
	/**
	 * Form string representation of expression.
	 * 
	 * @param node Current node of the expression tree
	 * @return Result of expression evaluation
	 */
	@SuppressWarnings("unchecked")
	protected String toString(Node node) {
		// encountered variable
		if (node.token instanceof Variable) {
			Variable variable = (Variable) node.token;
			return variable.label;
		}
		
		// encountered function
		else if (node.token instanceof Function) {
			Function<T> function = (Function<T>) node.token;
			
			int operandCount = function.parameters;
			if (node.children.size() != operandCount) {
				throw new Expr4jException("Invalid expression");
			}
			
			String operands = node.children.stream().map(this::toString).collect(Collectors.joining(", "));
			
			return function.label + "(" + operands + ")";
		}
		
		// encountered operator
		else if (node.token instanceof Operator) {
			Operator<T> operator = (Operator<T>) node.token;
			
			int operandCount = (operator.operatorType == OperatorType.INFIX || operator.operatorType == OperatorType.INFIX_RTL) ? 2 : 1;
			if (node.children.size() != operandCount) {
				throw new Expr4jException("Invalid expression");
			}
			
			String label = null;
			if (operator.label.equals(Operator.UNARY_PLUS)) label = "+";
			else if (operator.label.equals(Operator.UNARY_MINUS)) label = "-";
			else if (operator.label.equals(Operator.IMPLICIT_MULTIPLICATION)) {
				Token left = node.children.get(0).token;
				Token right = node.children.get(1).token;

				if (left instanceof Operand && right instanceof Operand) {
					label = " * ";
				}
				else if (left instanceof Operand && right instanceof Operator) {
					Operator<T> rightOperator = (Operator<T>) right;
					if (rightOperator.label.equals(Operator.UNARY_PLUS) || rightOperator.label.equals(Operator.UNARY_MINUS)) {
						label = " * ";
					}
					else {
						label = " ";
					}
				}
				else if (left instanceof Operator && right instanceof Operand) {
					Operator<T> leftOperator = (Operator<T>) left;
					if (leftOperator.label.equals(Operator.UNARY_PLUS) || leftOperator.label.equals(Operator.UNARY_MINUS)) {
						label = " * ";
					}
					else {
						label = " ";
					}
				}
				else label = " ";
			}
			else if (operandCount == 2) label = " " + operator.label + " ";
			else label = operator.label;
			
			if (operandCount == 2) {
				StringBuilder sb = new StringBuilder();
				
				Node left = node.children.get(0);
				Node right = node.children.get(1);

				if (left.token instanceof Operator) {
					Operator<T> leftOperator = (Operator<T>) left.token;
					if (leftOperator.label != Operator.IMPLICIT_MULTIPLICATION &&
							(leftOperator.operatorType == OperatorType.INFIX || leftOperator.operatorType == OperatorType.INFIX_RTL)) {
						sb.append("(");
						sb.append(this.toString(left));
						sb.append(")");
					}
					else {
						sb.append(this.toString(left));
					}
				}
				else {
					sb.append(this.toString(left));
				}
				
				sb.append(label);

				if (right.token instanceof Operator) {
					Operator<T> rightOperator = (Operator<T>) right.token;
					if (rightOperator.label != Operator.IMPLICIT_MULTIPLICATION &&
							(rightOperator.operatorType == OperatorType.INFIX || rightOperator.operatorType == OperatorType.INFIX_RTL)) {
						sb.append("(");
						sb.append(this.toString(right));
						sb.append(")");
					}
					else {
						sb.append(this.toString(right));
					}
				}
				else {
					sb.append(this.toString(right));
				}
				
				return sb.toString();
			}
			else {
				Node child = node.children.get(0);
				if (operator.label.equals(Operator.UNARY_PLUS) || operator.label.equals(Operator.UNARY_MINUS)) {
					if (child.token instanceof Operator) {
						Operator<T> childOperator = (Operator<T>) child.token;
						if (childOperator.operatorType == OperatorType.PREFIX) {
							return label + this.toString(child);
						}
						else {
							return label + "(" + this.toString(child) + ")";
						}
					}
					else {
						return label + this.toString(child);
					}
				}
				else if (child.token instanceof Operator || child.token instanceof Function) {
					if (operator.operatorType == OperatorType.PREFIX) {
						return label + "(" + this.toString(child) + ")";
					}
					else {
						return "(" + this.toString(child) + ") " + label;
					}
				}
				else {
					if (operator.operatorType == OperatorType.PREFIX) {
						return label + " " + this.toString(child);
					}
					else {
						return this.toString(child) + " " + label;
					}
				}
			}
		}
		
		// encountered operand
		else {
			Operand<T> operand = (Operand<T>) node.token;
			return operandRepresentation.toString(operand.value);
		}
	}

	/**
	 * Get string representation of expression.
	 */
	@Override
	public String toString() {
		return this.toString(root);
	}
	
	/**
	 * The <code>OperandRepresentation&lt;T&gt;</code> functional interface represents a string representation of an operand.
	 * 
	 * @author Pratanu Mandal
	 * @since 1.0
	 *
	 * @param <T> The type of operand
	 */
	@FunctionalInterface
	public interface OperandRepresentation<T> {
		
		/**
		 * Get string representation of operand.
		 * 
		 * @param value Value of operand
		 * @return String representation of operand
		 */
		public abstract String toString(T value);

	}
	
}
