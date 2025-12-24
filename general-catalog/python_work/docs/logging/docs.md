### Log Level Filtering Order:

1. **Logger Level (e.g., child logger)**

   * The logger first decides if a log record should be processed **based on its own level**.
   * If the log level is lower than the logger’s level, it is discarded.

2. **Handler Level (attached to the logger)**

   * If the logger accepts the record, it is then passed to its **handlers**.
   * Each handler again checks the log level and may **filter out** the record if it’s below the handler’s level.

3. **Logger Hierarchy (parent/root loggers)**

   * If `propagate=True` (default), the record is passed up to **parent loggers**, where the process repeats:

     * Parent logger level is checked.
     * Parent handlers are invoked (if any), applying their own level filters.

---

### Summary:

**LogRecord Filtering Order:**

`Child Logger Level -> Child Handler Level -> Parent Logger Level -> Parent Handler Level -> Root Logger Level -> Root Handler Level`

---

### Best Practices:

1. Put all handlers/filters/formatters on the root logger
2. Don't use root logger directly in your code -> use child loggers instead and propagate log records to root logger
3. If your project is large, use one logger per major module/subcomponent -> don't getLogger(__name__) in every file, but rather use a single logger per module

### References:
- [Modern Python logging tips](https://www.youtube.com/watch?v=9L77QExPmI0&list=LL&index=1&t=619s)
- [Understanding logging in Python](https://gist.github.com/mariocj89/73824162a3e35d50db8e758a42e39aab)

---

### A handful of rules for logging:
- DO include a timestamp
- DO format in JSON
- DON’T log insignificant events
- DO log all application errors
- MAYBE log warnings
- DO turn on logging
- DO write messages in a human-readable form
- DON’T log informational data in production
- DON’T log anything a human can’t read or react to