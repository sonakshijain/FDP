package templateStrings;

public class Templates {

    public static final String PASSWORD_RECOVERY_HTML = "<p style=\"text-align: center;\">\n" +
            "\t\t\t<span style=\"font-family: georgia, serif; font-size: 26px;\">${name}, you requested for a Password Reset.</span></p>\n" +
            "\t\t<p style=\"text-align: center;\">\n" +
            "\t\t\t<span style=\"font-size:11px;\"><span style=\"font-family:georgia,serif;\">In case you didn&#39;t, please ignore this email.</span></span></p>\n" +
            "\t\t<p style=\"text-align: center;\">\n" +
            "\t\t\t<span style=\"font-size:22px;\"><span style=\"font-family:georgia,serif;\">Here&#39;s the code: <span style=\"font-family:courier new,courier,monospace;\">${code}</span></span></span></p>\n" +
            "\t\t<p style=\"text-align: center;\">\n" +
            "\t\t\tPlease enter this code in the application to reset your password.</p>";
}
