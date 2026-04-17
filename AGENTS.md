# AGENTS.md

## Project-Specific Instructions

- Before changing bot flow architecture, reread this file.
- Keep `Promt.md`, `Status.md`, and `README.md` updated when behavior, schema, deployment, or operational instructions change.
- Conversation flows must go through `ConversationStep` + `MessageHandlerProvider`.
- Do not add `ConversationStep`-specific branches to `PrivateChatUpdateHandler`; if a Telegram update type is needed for a step, add it to `MessageHandlerContext` and handle it in the step handler.
- Admin message handlers live in `su.msk.nlx2.challengebot.service.bot.message.admin`.
- User message handlers live in `su.msk.nlx2.challengebot.service.bot.message.user`.
- Shared handler infrastructure stays in `su.msk.nlx2.challengebot.service.bot.message`.
- Project targets Java 25.
- Use `./mvnw` for local Maven commands. The wrapper pins Maven 3.9.14 and avoids Maven/Guice `sun.misc.Unsafe` warnings seen with Maven 3.9.11 on JDK 25.
- Java line length limit is 150 characters.
- If a method declaration or method call with all parameters fits into one line within 150 characters, keep it on one line.
- If a method declaration or method call does not fit into one line within 150 characters, put each parameter/argument on its own line.
- Do not use `var` in Java code; use explicit types to keep code readable and clear.
