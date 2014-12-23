package filter.lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import filter.ParseException;

public class Lexer {

	private final boolean SKIP_WHITESPACE = true;
	private final Pattern NO_WHITESPACE = Pattern.compile("\\S");
	
	private List<Rule> rules = Arrays.asList(
			new Rule("AND|&&?", TokenType.AND),
			new Rule("OR|\\|\\|?", TokenType.OR),
			new Rule("NOT|~|!|-", TokenType.NOT),
			new Rule("[A-Za-z]+\\s*:", TokenType.QUALIFIER), // This must be before Symbol
			new Rule("[A-Za-z0-9#][A-Za-z0-9.'-]*", TokenType.SYMBOL),
			new Rule("\\(", TokenType.LBRACKET),
			new Rule("\\)", TokenType.RBRACKET),
			new Rule("\\\"", TokenType.QUOTE),
			new Rule("\\.\\.", TokenType.DOTDOT),
			new Rule("<", TokenType.LT),
			new Rule("<=", TokenType.LTE),
			new Rule(">", TokenType.GT),
			new Rule(">=", TokenType.GTE),
			new Rule("\\*", TokenType.STAR),
			new Rule("\\d{4}-\\d{2}-\\d{2}", TokenType.DATE) // YYYY-MM-DD
		);

	private String input;
	private int position;
	
	public Lexer(String input) {
		this.input = input;
		this.position = 0;
	}

	private Token nextToken() {
		
		if (position >= input.length()) {
			return new Token(TokenType.EOF, "", position);
		}

		if (SKIP_WHITESPACE) {
			Matcher matcher = NO_WHITESPACE.matcher(input).region(position, input.length());
			boolean found = matcher.find();
			if (!found) {
				return new Token(TokenType.EOF, "", position);
			}
			position = matcher.start();
		}
		
		for (Rule r : rules) {
			Matcher matcher = r.getPattern().matcher(input).region(position, input.length());

			if (matcher.lookingAt()) {
				String match = matcher.group();
				position += match.length();

				return new Token(r.getTokenType(), match, matcher.start());
			}
		}
		throw new ParseException("Unrecognised token " + input.charAt(position) + " at " + position);
	}
	
	public ArrayList<Token> lex() {
		ArrayList<Token> result = new ArrayList<>();
		
		Token previous = null;
		while (position < input.length()
				&& (previous == null || previous.getType() != TokenType.EOF)) {
			previous = nextToken();
			result.add(previous);
		}
		result.add(nextToken()); // EOF

		return result;
	}

}
