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

import in.pratanumandal.expr4j.Expression.Node;
import in.pratanumandal.expr4j.exception.Expr4jException;
import in.pratanumandal.expr4j.token.Function;
import in.pratanumandal.expr4j.token.Operator;
import in.pratanumandal.expr4j.token.OperatorType;
import in.pratanumandal.expr4j.token.Token;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * The <code>ExpressionBuilder&lt;T&gt;</code> class provides a partial implementation to build expressions independent of the type of operand.<br>
 * An expression is created from the postfix (or RPN) expression. The expression can then be evaluated.<br><br>
 * 
 * @author Pratanu Mandal
 * @since 1.0
 *
 * @param <T> The type of operand for this parser
 */
public abstract class ExpressionBuilder<T> {
	
	/**
	 * Instance of expression.
	 */
	private Expression<T> expression;
	
	/**
	 * Expression dictionary.
	 */
	private ExpressionDictionary<T> expressionDictionary;
	
	/**
	 * No-Argument Constructor.
	 */
	public ExpressionBuilder() {
		this.reset();
	}

	/**
	 * Reset the parser.
	 */
	public void reset() {
		expressionDictionary = new ExpressionDictionary<>();
	}

	/**
	 * Method to form the expression tree recursively.
	 *
	 * @param node Current node of the expression tree
	 * @param token Token to be inserted
	 * @return true if token could be inserted, otherwise false
	 */
	@SuppressWarnings("unchecked")
	private boolean formTree(Node node, Token token) {
		if (node.token instanceof Function) {
			Function<T> function = (Function<T>) node.token;

			int operandCount = function.parameters;

			if (node.children.size() > 0 && formTree(node.children.get(0), token)) {
				return true;
			}
			else if (node.children.size() < operandCount) {
				node.children.add(0, new Node(token));
				return true;
			}
		}
		else if (node.token instanceof Operator) {
			Operator<T> operator = (Operator<T>) node.token;

			int operandCount = (operator.type == OperatorType.INFIX || operator.type == OperatorType.INFIX_RTL) ? 2 : 1;

			if (node.children.size() > 0 && formTree(node.children.get(0), token)) {
				return true;
			}
			else if (node.children.size() < operandCount) {
				node.children.add(0, new Node(token));
				return true;
			}
		}

		return false;
	}

	/**
	 * Method to form the expression tree.
	 */
	private void formTree(Stack<Token> postfix) {
		while (!postfix.isEmpty()) {
			Token token = postfix.pop();

			if (expression.root == null) {
				Node node = new Node(token);
				expression.root = node;
			}
			else {
				boolean flag = formTree(expression.root, token);

				if (!flag) {
					throw new Expr4jException("Invalid expression");
				}
			}
		}
	}

	/**
	 * Method to parse an expression.<br>
	 * This method acts as the single point of access for expression parsing.
	 *
	 * @param expr Expression string
	 * @return The parsed expression
	 */
	public Expression<T> build(String expr) {
		try {
			// initialize expression
			this.expression = new Expression<>(expressionDictionary, this::operandToString);

			// tokenize the expression
			ExpressionTokenizer<T> tokenizer = new ExpressionTokenizer<T>(expressionDictionary) {
				@Override
				protected T stringToOperand(String operand) {
					return ExpressionBuilder.this.stringToOperand(operand);
				}

				@Override
				protected List<String> getNumberPattern() {
					return ExpressionBuilder.this.getNumberPattern();
				}
			};
			List<Token> tokenList = tokenizer.tokenize(expr);

			// form the postfix expression
			ExpressionParser<T> parser = new ExpressionParser<T>();
			Stack<Token> postfix = parser.parse(tokenList);

			// form the tree
			this.formTree(postfix);
			
			return this.expression;
		}
		finally {
			// clean up - expression evaluation can be a memory intensive process
			this.expression = null;
		}
	}

	/**
	 * Get the expression dictionary.
	 *
	 * @return The expression dictionary
	 */
	public ExpressionDictionary<T> getExpressionDictionary() {
		return expressionDictionary;
	}
	
	/**
	 * Method to define procedure to obtain operand from string representation.
	 * 
	 * @param operand String representation of operand
	 * @return Operand
	 */
	protected abstract T stringToOperand(String operand);
	
	/**
	 * Method to define procedure to obtain string representation of operand.
	 * 
	 * @param operand Operand
	 * @return String representation of operand
	 */
	protected abstract String operandToString(T operand);
	
	/**
	 * Method to define the patterns to identify numbers.<br>
	 * Override this method if the patterns to identify numbers need to be customized.
	 * 
	 * @return List of patterns to identify numbers
	 */
	protected List<String> getNumberPattern() {
		return Arrays.asList("(-?\\d+)(\\.\\d+)?(e-|e\\+|e|\\d+)\\d+", "\\d*\\.?\\d+");
	}
	
}
