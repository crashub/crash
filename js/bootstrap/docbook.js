$(document).ready(function() {

    // Topbar
    var topbar =
            "<div class='topbar'>" +
                    "<div class='fill'>" +
                    "<div class='container'>" +
                    "<ul id='topbar'>" +
                    "</ul>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
    $("div.book").before(topbar);
    $("body").css("padding-top", "40px");

    //
    var title = $("div.titlepage h1.title");
    if (title.length == 1) {
        var titleLink = $("<a class='brand' href='#'></a>").appendTo("#topbar");
        titleLink.text(title.text());
    }

    //
    var createModal = function(id, heading, elements) {

        // Link in topbar
        var tocLink = $("<li>" +
                "<a href='#' data-controls-modal='" + id + "' data-backdrop='true' data-keyboard='true'>" + heading + "</a>" +
                "</li>").appendTo("#topbar");

        // The modal window
        var tocModal = $("<div id='" + id + "' class='modal hide'>" +
                "<div class='modal-header'>" +
                "<a href='#' class='close'>&times;</a>" +
                "<h3>" + heading + "</h3>" +
                "</div>" +
                "<div class='modal-body' style='height:512px;overflow:auto'></div>" +
                "<div class='modal-footer'></div>" +
                "</div>");
        $("div.book").before(tocModal);
        var body = tocModal.find(".modal-body");
        elements.appendTo(body);
        body.find("a").click(function() { tocModal.modal("hide"); });
    }

    // Table of Content
    if ($('div.book > div.toc').length == 1) {
        createModal("modal-toc", "Table of Content", $("div.book > div.toc > ul").addClass("unstyled").clone());
        $(".toc").remove();
    }

    // Lit of Figures
    if ($('div.book > div.list-of-figures').length == 1) {
        createModal("modal-figures", "List of Figures", $("div.book > div.list-of-figures > ul").addClass("unstyled").clone());
        $(".list-of-figures").remove();
    }

    // List of Tables
    if ($('div.book > div.list-of-tables').length == 1) {
        createModal("modal-tables", "List of Tables", $("div.book > div.list-of-tables > ul").addClass("unstyled").clone());
        $(".list-of-tables").remove();
    }

    // List of Examles
    if ($('div.book > div.list-of-examples').length == 1) {
        createModal("modal-examples", "List of Examples", $("div.book > div.list-of-examples > ul").addClass("unstyled").clone());
        $(".list-of-examples").remove();
    }

    // Container for book
    $(".book").addClass("container");

    // Admonitions
    $(".warning").addClass("alert-message").addClass("block-message");
    $(".note").addClass("success").addClass("alert-message").addClass("block-message");
    $(".tip").addClass("info").addClass("alert-message").addClass("block-message");
    $(".important").addClass("error").addClass("alert-message").addClass("block-message");
    $(".caution").addClass("warning").addClass("alert-message").addClass("block-message");

    // Pretty print
    $(".programlisting").addClass("prettyprint");
    prettyPrint();
});
