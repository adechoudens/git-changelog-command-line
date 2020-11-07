package se.bjurr.gitchangelog.main;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.of;
import static se.bjurr.gitchangelog.api.GitChangelogApi.gitChangelogApiBuilder;
import static se.bjurr.gitchangelog.api.GitChangelogApiConstants.DEFAULT_DATEFORMAT;
import static se.bjurr.gitchangelog.internal.settings.Settings.defaultSettings;
import static se.softhouse.jargo.Arguments.helpArgument;
import static se.softhouse.jargo.Arguments.optionArgument;
import static se.softhouse.jargo.Arguments.stringArgument;
import static se.softhouse.jargo.CommandLineParser.withArguments;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import se.bjurr.gitchangelog.api.GitChangelogApi;
import se.bjurr.gitchangelog.internal.settings.Settings;
import se.softhouse.jargo.Argument;
import se.softhouse.jargo.ArgumentException;
import se.softhouse.jargo.ParsedArguments;

public class Main {
  public static final String PARAM_SETTINGS_FILE = "-sf";
  public static final String PARAM_OUTPUT_FILE = "-of";
  public static final String PARAM_OUTPUT_STDOUT = "-std";
  public static final String PARAM_TEMPLATE = "-t";
  public static final String PARAM_REPO = "-r";
  public static final String PARAM_FROM_REF = "-fr";
  public static final String PARAM_TO_REF = "-tr";
  public static final String PARAM_FROM_COMMIT = "-fc";
  public static final String PARAM_TO_COMMIT = "-tc";
  public static final String PARAM_IGNORE_PATTERN = "-ip";
  public static final String PARAM_IGNORE_OLDER_PATTERN = "-iot";
  public static final String PARAM_IGNORE_TAG_PATTERN = "-itp";
  public static final String PARAM_JIRA_SERVER = "-js";
  public static final String PARAM_JIRA_ISSUE_PATTERN = "-jp";
  public static final String PARAM_JIRA_USERNAME = "-ju";
  public static final String PARAM_JIRA_PASSWORD = "-jpw";
  public static final String PARAM_JIRA_BASIC_AUTH = "-jba";
  public static final String PARAM_CUSTOM_ISSUE_NAME = "-cn";
  public static final String PARAM_CUSTOM_ISSUE_PATTERN = "-cp";
  public static final String PARAM_CUSTOM_ISSUE_LINK = "-cl";
  public static final String PARAM_CUSTOM_ISSUE_TITLE = "-ct";
  public static final String PARAM_UNTAGGED_TAG_NAME = "-ut";
  public static final String PARAM_TIMEZONE = "-tz";
  public static final String PARAM_DATEFORMAT = "-df";
  public static final String PARAM_NOISSUE = "-ni";
  public static final String PARAM_IGNORE_NOISSUE = "-ini";
  public static final String PARAM_READABLETAGNAME = "-rt";
  public static final String PARAM_REMOVEISSUE = "-ri";
  public static final String PARAM_MEDIAWIKIURL = "-murl";
  public static final String PARAM_MEDIAWIKITITLE = "-mt";
  public static final String PARAM_MEDIAWIKIUSER = "-mu";
  public static final String PARAM_MEDIAWIKIPASSWORD = "-mp";
  public static final String PARAM_GITHUBAPI = "-gapi";
  public static final String PARAM_GITHUBTOKEN = "-gtok";
  public static final String PARAM_EXTENDED_VARIABLES = "-ex";
  public static final String PARAM_EXTENDED_HEADERS = "-eh";
  public static final String PARAM_TEMPLATE_CONTENT = "-tec";
  public static final String PARAM_GITLABTOKEN = "-glt";
  public static final String PARAM_GITLABSERVER = "-gls";
  public static final String PARAM_GITLABPROJECTNAME = "-glpn";

  private static String systemOutPrintln;
  private static boolean recordSystemOutPrintln;

  public static void main(String args[]) throws Exception {
    Settings defaultSettings = defaultSettings();
    Argument<?> helpArgument = helpArgument("-h", "--help");

    Argument<String> settingsArgument =
        stringArgument(PARAM_SETTINGS_FILE, "--settings-file") //
            .description("Use settings from file.") //
            .defaultValue(null) //
            .build();
    Argument<Boolean> outputStdoutArgument =
        optionArgument(PARAM_OUTPUT_STDOUT, "--stdout") //
            .description("Print builder to <STDOUT>.") //
            .build();
    Argument<String> outputFileArgument =
        stringArgument(PARAM_OUTPUT_FILE, "--output-file") //
            .description("Write output to file.") //
            .build();

    Argument<String> templatePathArgument =
        stringArgument(PARAM_TEMPLATE, "--template") //
            .description("Template to use. A default template will be used if not specified.") //
            .defaultValue(defaultSettings.getTemplatePath()) //
            .build();

    Argument<String> untaggedTagNameArgument =
        stringArgument(PARAM_UNTAGGED_TAG_NAME, "--untagged-name") //
            .description(
                "When listing commits per tag, this will by the name of a virtual tag that contains commits not available in any git tag.") //
            .defaultValue(defaultSettings.getUntaggedName()) //
            .build();

    Argument<String> fromRepoArgument =
        stringArgument(PARAM_REPO, "--repo") //
            .description("Repository.") //
            .defaultValue(defaultSettings.getFromRepo()) //
            .build();
    Argument<String> fromRefArgument =
        stringArgument(PARAM_FROM_REF, "--from-ref") //
            .description("From ref.") //
            .defaultValue(defaultSettings.getFromRef().orNull()) //
            .build();
    Argument<String> toRefArgument =
        stringArgument(PARAM_TO_REF, "--to-ref") //
            .description("To ref.") //
            .defaultValue(defaultSettings.getToRef().orNull()) //
            .build();
    Argument<String> fromCommitArgument =
        stringArgument(PARAM_FROM_COMMIT, "--from-commit") //
            .description("From commit.") //
            .defaultValue(defaultSettings.getFromCommit().orNull()) //
            .build();
    Argument<String> toCommitArgument =
        stringArgument(PARAM_TO_COMMIT, "--to-commit") //
            .description("To commit.") //
            .defaultValue(defaultSettings.getToCommit().orNull()) //
            .build();

    Argument<String> ignoreCommitsIfMessageMatchesArgument =
        stringArgument(PARAM_IGNORE_PATTERN, "--ignore-pattern") //
            .description("Ignore commits where pattern matches message.") //
            .defaultValue(defaultSettings.getIgnoreCommitsIfMessageMatches()) //
            .build();

    Argument<String> ignoreCommitsOlderThanArgument =
        stringArgument(PARAM_IGNORE_OLDER_PATTERN, "--ignore-older-than") //
            .description("Ignore commits older than " + DEFAULT_DATEFORMAT + ".") //
            .build();

    Argument<String> ignoreTagsIfNameMatchesArgument =
        stringArgument(PARAM_IGNORE_TAG_PATTERN, "--ignore-tag-pattern") //
            .description(
                "Ignore tags that matches regular expression. Can be used to ignore release candidates and only include actual releases.") //
            .defaultValue(defaultSettings.getIgnoreTagsIfNameMatches().orNull()) //
            .build();

    Argument<String> jiraServerArgument =
        stringArgument(PARAM_JIRA_SERVER, "--jiraServer") //
            .description(
                "Jira server. When a Jira server is given, the title of the Jira issues can be used in the changelog.") //
            .defaultValue(defaultSettings.getJiraServer().orNull()) //
            .build();
    Argument<String> jiraIssuePatternArgument =
        stringArgument(PARAM_JIRA_ISSUE_PATTERN, "--jira-pattern") //
            .description("Jira issue pattern.") //
            .defaultValue(defaultSettings.getJiraIssuePattern()) //
            .build();
    Argument<String> jiraUsernamePatternArgument =
        stringArgument(PARAM_JIRA_USERNAME, "--jira-username") //
            .description("Optional username to authenticate with Jira.") //
            .defaultValue(defaultSettings.getJiraIssuePattern()) //
            .build();
    Argument<String> jiraPasswordPatternArgument =
        stringArgument(PARAM_JIRA_PASSWORD, "--jira-password") //
            .description("Optional password to authenticate with Jira.") //
            .defaultValue(defaultSettings.getJiraIssuePattern()) //
            .build();
    Argument<String> jiraBasicAuthStringPatternArgument =
        stringArgument(PARAM_JIRA_BASIC_AUTH, "--jira-basic-auth") //
            .description("Optional token to authenticate with Jira.") //
            .defaultValue(defaultSettings.getJiraIssuePattern()) //
            .build();

    Argument<String> customIssueNameArgument =
        stringArgument(PARAM_CUSTOM_ISSUE_NAME, "--custom-issue-name") //
            .description("Custom issue name.") //
            .defaultValue(null) //
            .build();
    Argument<String> customIssuePatternArgument =
        stringArgument(PARAM_CUSTOM_ISSUE_PATTERN, "--custom-issue-pattern") //
            .description("Custom issue pattern.") //
            .defaultValue(null) //
            .build();
    Argument<String> customIssueLinkArgument =
        stringArgument(PARAM_CUSTOM_ISSUE_LINK, "--custom-issue-link") //
            .description(
                "Custom issue link. Supports variables like ${PATTERN_GROUP_1} to inject variables from pattern.") //
            .defaultValue(null) //
            .build();
    Argument<String> customIssueTitleArgument =
        stringArgument(PARAM_CUSTOM_ISSUE_TITLE, "--custom-issue-title") //
            .description(
                "Custom issue title. Supports variables like ${PATTERN_GROUP_1} to inject variables from pattern.") //
            .defaultValue(null) //
            .build();

    Argument<String> timeZoneArgument =
        stringArgument(PARAM_TIMEZONE, "--time-zone") //
            .description("TimeZone to use when printing dates.") //
            .defaultValue(defaultSettings.getTimeZone()) //
            .build();
    Argument<String> dateFormatArgument =
        stringArgument(PARAM_DATEFORMAT, "--date-format") //
            .description("Format to use when printing dates.") //
            .defaultValue(defaultSettings.getDateFormat()) //
            .build();
    Argument<String> noIssueArgument =
        stringArgument(PARAM_NOISSUE, "--no-issue-name") //
            .description(
                "Name of virtual issue that contains commits that has no issue associated.") //
            .defaultValue(defaultSettings.getNoIssueName()) //
            .build();
    Argument<Boolean> ignoreCommitsWithoutIssueArgument =
        optionArgument(PARAM_IGNORE_NOISSUE, "--ignore-commits-without-issue") //
            .description("Ignore commits that is not included in any issue.") //
            .build();
    Argument<String> readableTagNameArgument =
        stringArgument(PARAM_READABLETAGNAME, "--readable-tag-name") //
            .description("Pattern to extract readable part of tag.") //
            .defaultValue(defaultSettings.getReadableTagName()) //
            .build();
    Argument<Boolean> removeIssueFromMessageArgument =
        optionArgument(PARAM_REMOVEISSUE, "--remove-issue-from-message") //
            .description("Dont print any issues in the messages of commits.") //
            .build();

    Argument<String> mediaWikiUrlArgument =
        stringArgument(PARAM_MEDIAWIKIURL, "--mediawiki-url") //
            .description("Base URL of MediaWiki.") //
            .build();
    Argument<String> mediaWikiTitleArgument =
        stringArgument(PARAM_MEDIAWIKITITLE, "--mediawiki-title") //
            .description("Title of MediaWiki page.") //
            .defaultValue(null) //
            .build();
    Argument<String> mediaWikiUserArgument =
        stringArgument(PARAM_MEDIAWIKIUSER, "--mediawiki-user") //
            .description("User to authenticate with MediaWiki.") //
            .defaultValue("") //
            .build();
    Argument<String> mediaWikiPasswordArgument =
        stringArgument(PARAM_MEDIAWIKIPASSWORD, "--mediawiki-password") //
            .description("Password to authenticate with MediaWiki.") //
            .defaultValue("") //
            .build();
    Argument<String> gitHubApiArgument =
        stringArgument(PARAM_GITHUBAPI, "--github-api") //
            .description(
                "GitHub API. Like: https://api.github.com/repos/tomasbjerre/git-changelog-command-line/") //
            .defaultValue("") //
            .build();
    Argument<String> gitHubTokenArgument =
        stringArgument(PARAM_GITHUBTOKEN, "--github-token") //
            .description(
                "GitHub API OAuth2 token. You can get it from: curl -u 'yourgithubuser' -d '{\"note\":\"Git Changelog Lib\"}' https://api.github.com/authorizations") //
            .defaultValue("") //
            .build();

    Argument<String> extendedVariablesArgument =
        stringArgument(PARAM_EXTENDED_VARIABLES, "--extended-variables") //
            .description(
                "Extended variables that will be available as {{extended.*}}. "
                    + PARAM_EXTENDED_VARIABLES
                    + " \"{\\\"var1\\\": \\\"val1\\\"}\" will print out \"val1\" for a template like \"{{extended.var1}}\"") //
            .defaultValue("") //
            .build();

    Argument<String> extendedHeadersArgument =
        stringArgument(PARAM_EXTENDED_HEADERS, "--extended-headers") //
            .description(
                "Extended headers that will send when access JIRA. e.g. "
                    + PARAM_EXTENDED_HEADERS
                    + " \"{\\\"CF-Access-Client-ID\\\": \\\"abcde12345xyz.access\\\"}\"") //
            .defaultValue("") //
            .build();

    Argument<String> templateContentArgument =
        stringArgument(PARAM_TEMPLATE_CONTENT, "--template-content") //
            .description("String to use as template.") //
            .defaultValue("") //
            .build();

    Argument<String> gitLabTokenArgument =
        stringArgument(PARAM_GITLABTOKEN, "--gitlab-token") //
            .description("GitLab API token.") //
            .defaultValue("") //
            .build();
    Argument<String> gitLabServerArgument =
        stringArgument(PARAM_GITLABSERVER, "--gitlab-server") //
            .description("GitLab server, like https://gitlab.com/.") //
            .defaultValue("") //
            .build();
    Argument<String> gitLabProjectNameArgument =
        stringArgument(PARAM_GITLABPROJECTNAME, "--gitlab-project-name") //
            .description("GitLab project name.") //
            .defaultValue("") //
            .build();

    try {
      ParsedArguments arg =
          withArguments(
                  helpArgument,
                  settingsArgument,
                  outputStdoutArgument,
                  outputFileArgument,
                  templatePathArgument,
                  fromCommitArgument,
                  fromRefArgument,
                  fromRepoArgument,
                  toCommitArgument,
                  toRefArgument,
                  untaggedTagNameArgument,
                  jiraIssuePatternArgument,
                  jiraServerArgument,
                  ignoreCommitsIfMessageMatchesArgument,
                  ignoreCommitsOlderThanArgument,
                  customIssueLinkArgument,
                  customIssueTitleArgument,
                  customIssueNameArgument,
                  customIssuePatternArgument,
                  timeZoneArgument,
                  dateFormatArgument,
                  noIssueArgument,
                  readableTagNameArgument,
                  removeIssueFromMessageArgument,
                  mediaWikiUrlArgument,
                  mediaWikiUserArgument,
                  mediaWikiPasswordArgument,
                  mediaWikiTitleArgument,
                  gitHubApiArgument,
                  jiraUsernamePatternArgument,
                  jiraPasswordPatternArgument,
                  jiraBasicAuthStringPatternArgument,
                  extendedVariablesArgument,
                  extendedHeadersArgument,
                  templateContentArgument,
                  gitHubTokenArgument,
                  ignoreCommitsWithoutIssueArgument,
                  ignoreTagsIfNameMatchesArgument,
                  gitLabTokenArgument,
                  gitLabServerArgument,
                  gitLabProjectNameArgument) //
              .parse(args);

      GitChangelogApi changelogApiBuilder = gitChangelogApiBuilder();

      if (arg.wasGiven(settingsArgument)) {
        changelogApiBuilder.withSettings(new File(arg.get(settingsArgument)).toURI().toURL());
      }

      if (arg.wasGiven(removeIssueFromMessageArgument)) {
        changelogApiBuilder.withRemoveIssueFromMessageArgument(true);
      }
      if (arg.wasGiven(ignoreCommitsWithoutIssueArgument)) {
        changelogApiBuilder.withIgnoreCommitsWithoutIssue(true);
      }

      if (arg.wasGiven(extendedVariablesArgument)) {
        String jsonString = arg.get(extendedVariablesArgument);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Object jsonObject = gson.fromJson(jsonString, type);
        Map<String, Object> extendedVariables = of("extended", jsonObject);
        changelogApiBuilder.withExtendedVariables(extendedVariables);
      }

      if (arg.wasGiven(extendedHeadersArgument)) {
        String jsonString = arg.get(extendedHeadersArgument);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> jsonObject = gson.fromJson(jsonString, type);
        changelogApiBuilder.withExtendedHeaders(jsonObject);
      }

      if (arg.wasGiven(templateContentArgument)) {
        changelogApiBuilder.withTemplateContent(arg.get(templateContentArgument));
      }

      if (arg.wasGiven(fromRepoArgument)) {
        changelogApiBuilder.withFromRepo(arg.get(fromRepoArgument));
      }
      if (arg.wasGiven(untaggedTagNameArgument)) {
        changelogApiBuilder.withUntaggedName(arg.get(untaggedTagNameArgument));
      }
      if (arg.wasGiven(ignoreCommitsIfMessageMatchesArgument)) {
        changelogApiBuilder.withIgnoreCommitsWithMessage(
            arg.get(ignoreCommitsIfMessageMatchesArgument));
      }
      if (arg.wasGiven(ignoreCommitsOlderThanArgument)) {
        Date date =
            new SimpleDateFormat(DEFAULT_DATEFORMAT).parse(arg.get(ignoreCommitsOlderThanArgument));
        changelogApiBuilder.withIgnoreCommitsOlderThan(date);
      }
      if (arg.wasGiven(ignoreTagsIfNameMatchesArgument)) {
        changelogApiBuilder.withIgnoreTagsIfNameMatches(arg.get(ignoreTagsIfNameMatchesArgument));
      }
      if (arg.wasGiven(templatePathArgument)) {
        changelogApiBuilder.withTemplatePath(arg.get(templatePathArgument));
      }
      if (arg.wasGiven(jiraIssuePatternArgument)) {
        changelogApiBuilder.withJiraIssuePattern(arg.get(jiraIssuePatternArgument));
      }
      if (arg.wasGiven(jiraServerArgument)) {
        changelogApiBuilder.withJiraServer(arg.get(jiraServerArgument));
      }
      if (arg.wasGiven(jiraUsernamePatternArgument)) {
        changelogApiBuilder.withJiraUsername(arg.get(jiraUsernamePatternArgument));
      }
      if (arg.wasGiven(jiraPasswordPatternArgument)) {
        changelogApiBuilder.withJiraPassword(arg.get(jiraPasswordPatternArgument));
      }
      if (arg.wasGiven(jiraBasicAuthStringPatternArgument)) {
        changelogApiBuilder.withJiraBasicAuthString(arg.get(jiraBasicAuthStringPatternArgument));
      }
      if (arg.wasGiven(timeZoneArgument)) {
        changelogApiBuilder.withTimeZone(arg.get(timeZoneArgument));
      }
      if (arg.wasGiven(dateFormatArgument)) {
        changelogApiBuilder.withDateFormat(arg.get(dateFormatArgument));
      }
      if (arg.wasGiven(noIssueArgument)) {
        changelogApiBuilder.withNoIssueName(arg.get(noIssueArgument));
      }
      if (arg.wasGiven(readableTagNameArgument)) {
        changelogApiBuilder.withReadableTagName(arg.get(readableTagNameArgument));
      }

      if (arg.wasGiven(fromCommitArgument)) {
        changelogApiBuilder.withFromCommit(arg.get(fromCommitArgument));
        changelogApiBuilder.withFromRef(null);
      }
      if (arg.wasGiven(fromRefArgument)) {
        changelogApiBuilder.withFromCommit(null);
        changelogApiBuilder.withFromRef(arg.get(fromRefArgument));
      }
      if (arg.wasGiven(toCommitArgument)) {
        changelogApiBuilder.withToCommit(arg.get(toCommitArgument));
        changelogApiBuilder.withToRef(null);
      }
      if (arg.wasGiven(toRefArgument)) {
        changelogApiBuilder.withToCommit(null);
        changelogApiBuilder.withToRef(arg.get(toRefArgument));
      }
      if (arg.wasGiven(gitHubApiArgument)) {
        changelogApiBuilder.withGitHubApi(arg.get(gitHubApiArgument));
      }
      if (arg.wasGiven(gitHubTokenArgument)) {
        changelogApiBuilder.withGitHubToken(arg.get(gitHubTokenArgument));
      }

      if (arg.wasGiven(gitLabServerArgument)) {
        changelogApiBuilder.withGitLabServer(arg.get(gitLabServerArgument));
      }
      if (arg.wasGiven(gitLabProjectNameArgument)) {
        changelogApiBuilder.withGitLabProjectName(arg.get(gitLabProjectNameArgument));
      }
      if (arg.wasGiven(gitLabTokenArgument)) {
        changelogApiBuilder.withGitLabToken(arg.get(gitLabTokenArgument));
      }

      if ( //
      arg.wasGiven(customIssueNameArgument)
          && //
          arg.wasGiven(customIssuePatternArgument)) {
        String title = null;
        if (arg.wasGiven(customIssueTitleArgument)) {
          title = arg.get(customIssueTitleArgument);
        }
        String link = null;
        if (arg.wasGiven(customIssueLinkArgument)) {
          link = arg.get(customIssueLinkArgument);
        }
        changelogApiBuilder.withCustomIssue( //
            arg.get(customIssueNameArgument), //
            arg.get(customIssuePatternArgument), //
            link, //
            title);
      }

      checkArgument( //
          arg.wasGiven(outputStdoutArgument)
              || arg.wasGiven(outputFileArgument)
              || arg.wasGiven(mediaWikiUrlArgument), //
          "You must supply an output, "
              + PARAM_OUTPUT_FILE
              + " <filename>, "
              + PARAM_OUTPUT_STDOUT
              + " or "
              + PARAM_MEDIAWIKIURL
              + " http://...");

      if (arg.wasGiven(mediaWikiUrlArgument)) {
        changelogApiBuilder //
            .toMediaWiki( //
            arg.get(mediaWikiUserArgument), //
            arg.get(mediaWikiPasswordArgument), //
            arg.get(mediaWikiUrlArgument), //
            arg.get(mediaWikiTitleArgument)); //
      }

      if (arg.wasGiven(outputStdoutArgument)) {
        systemOutPrintln(changelogApiBuilder.render());
      }

      if (arg.wasGiven(outputFileArgument)) {
        String filePath = arg.get(outputFileArgument);
        changelogApiBuilder.toFile(new File(filePath));
      }

    } catch (ArgumentException exception) {
      System.out.println(exception.getMessageAndUsage());
      System.exit(1);
    }
  }

  @VisibleForTesting
  public static String getSystemOutPrintln() {
    return Main.systemOutPrintln;
  }

  @VisibleForTesting
  public static void recordSystemOutPrintln() {
    Main.recordSystemOutPrintln = true;
  }

  private static void systemOutPrintln(String systemOutPrintln) {
    if (Main.recordSystemOutPrintln) {
      Main.systemOutPrintln = systemOutPrintln;
    } else {
      System.out.println(systemOutPrintln);
    }
  }
}
