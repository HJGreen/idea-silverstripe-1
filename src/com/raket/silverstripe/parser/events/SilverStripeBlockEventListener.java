package com.raket.silverstripe.parser.events;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.raket.silverstripe.parser.SilverStripeBaseParser;

import static com.raket.silverstripe.psi.SilverStripeTypes.*;

public class SilverStripeBlockEventListener extends SilverStripeStatementEventListener {
	private IElementType blockType;
	private TokenSet myStartTokens;

	@Override
	public void updateBefore(SilverStripeBaseParser parser, IElementType token) {
		currentToken = token;

		if (token != null && token.equals(SS_BLOCK_START) && !marking) {
			nextToken = parser.getNextToken();
			myStartTokens = STATEMENT_MAP.get(nextToken);
			if (myStartTokens != null) {
				builder = parser.getBuilder();
				tokenText = parser.getNextTokenText();
				observerMarker = builder.mark();
				marking = true;
			}
		}
	}

	@Override
	public void updateAfter(SilverStripeBaseParser parser, IElementType token) {
		if (marking && currentToken.equals(SS_BLOCK_END)) {
			blockType = BLOCK_TYPE_MAP.get(nextToken);
			observerMarker.done(blockType);

			if (BLOCK_STATEMENTS.contains(blockType))
				dispatcher.triggerEvent("block_start_statement_complete", false, this, tokenText);
/*
			else if (blockType.equals(SS_ELSE_IF_STATEMENT) || blockType.equals(SS_ELSE_STATEMENT))
				dispatcher.triggerEvent("if_continue_statement_complete", false, this, tokenText);
*/
			else if (blockType.equals(SS_BLOCK_END_STATEMENT))
				dispatcher.triggerEvent("block_end_statement_complete", false, this, tokenText);

			marking = false;
		}
		if (marking && !myStartTokens.contains(currentToken)) {
			observerMarker.drop();
			marking = false;
		}

		super.updateAfter(parser, token);
	}
}
