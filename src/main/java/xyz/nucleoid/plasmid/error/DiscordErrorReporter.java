package xyz.nucleoid.plasmid.error;

import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

final class DiscordErrorReporter implements ErrorReporter {
    private static final int MAX_CHARACTER_LIMIT = 2000;

    private final String source;
    private final DiscordWebhook webhook;

    private final List<ReportedError> reportedErrors = new ArrayList<>();

    DiscordErrorReporter(String source, DiscordWebhook webhook) {
        this.source = source;
        this.webhook = webhook;
    }

    @Override
    public void report(Throwable throwable, @Nullable String context) {
        this.reportedErrors.add(new ReportedError(throwable, context));
    }

    @Override
    public void close() {
        if (this.reportedErrors.isEmpty()) {
            return;
        }

        List<ReportedError> errors = this.reportedErrors;
        int displayCount = Math.min(errors.size(), 20);

        StringBuilder content = new StringBuilder();
        content.append(":warning: Reporting ").append(displayCount).append(" errors from: **").append(this.source).append("** :warning:\n");

        if (displayCount < errors.size()) {
            content.append("_Skipping ").append(errors.size() - displayCount).append(" errors..._\n");
        }

        content.append('\n');

        StringBuilder errorsContent = new StringBuilder();

        for (int i = 0; i < displayCount; i++) {
            ReportedError error = errors.get(i);
            StringWriter traceWriter = new StringWriter();
            error.cause.printStackTrace(new PrintWriter(traceWriter));

            if (error.context != null) {
                errorsContent.append("**").append(error.context).append("**\n");
            }

            errorsContent.append("```\n");
            errorsContent.append(traceWriter.toString());
            errorsContent.append("```\n");
        }

        if (content.length() + errorsContent.length() < MAX_CHARACTER_LIMIT) {
            content.append(errorsContent);
            this.webhook.post(new DiscordWebhook.Message(content.toString()));
        } else {
            content.append("Traces have been attached.");
            this.webhook.post(new DiscordWebhook.Message(content.toString()).addFile("trace.txt", errorsContent.toString()));
        }
    }

    private static class ReportedError {
        private final Throwable cause;
        private final String context;

        ReportedError(Throwable cause, String context) {
            this.cause = cause;
            this.context = context;
        }
    }
}
