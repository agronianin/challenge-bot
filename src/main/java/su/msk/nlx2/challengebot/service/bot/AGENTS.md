# Telegram-flow

- `UpdateHandler` выполняет верхнеуровневую маршрутизацию; пользователя и локаль разрешает `BotRequestContextResolver`.
- Пользователь и администратор используют общий `ConversationService` / `ConversationSession` / `ConversationStep`; административные возможности — дополнительные ветки общего flow.
- Каждый шаг обрабатывается через `MessageHandlerProvider` и отдельный `MessageHandler`. Не добавляй ветвления по конкретному `ConversationStep` в `PrivateChatUpdateHandler`: текст, document, `usersShared`, `chatShared` передавай через `MessageHandlerContext`.
- Обработчики шагов администратора находятся в `message.admin`, пользователя — в `message.user`, общая инфраструктура — в `message` относительно `service.bot`.
- Действия inline-меню маршрутизируй через providers и handlers в `callback.admin` / `callback.user`. Клавиатуры размещай в `keyboard`; бизнес-логику действий не помещай в `PrivateChatUpdateHandler`.
- Тексты и кнопки локализуй через `BotMessages` / `MessageSource` и `messages_*.properties`. Технические `callback_data`, action ID и ключи не зависят от языка.
- Используй разрешенные роль, Telegram ID и локаль из `TgUser`, без отдельного дублирующего контекста пользователя.
