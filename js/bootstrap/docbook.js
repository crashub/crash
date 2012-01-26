$(document).ready(function() {

    // Topbar
    var topbar =
            "<div class='topbar'>" +
                    "<div class='fill'>" +
                    "<div class='container'>" +
                    "<ul id='topbar'>" +
                    "</ul>" +
                    "<ul class='nav secondary-nav' class='dropdown' data-dropdown='dropdown'>" +
                    "<li class='menu'>" +
                    "<a href='#' class='dropdown-toggle'>Powered by Wikbook</a>" +
                    "<ul class='menu-dropdown'>" +
                    "<li><a href='http://www.github.org/vietj/wikbook'>Project</a></li>" +
                    "<li><a href='http://vietj.github.com/wikbook/'>Documentation</a></li>" +
                    "<li><a href='http://jira.exoplatform.org/browse/WKBK'>Issue tracker</a></li>" +
                    "</ul>" +
                    "</li>" +
                    "</ul>" +
                    "</li>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
    $("div.book").before(topbar);
    $("body").css("padding-top", "40px");

    //
    var addModal = function(id, heading, elements, bodyClass) {
        // The modal window
        var modal = $("<div id='" + id + "' class='modal hide'>" +
                "<div class='modal-header'>" +
                "<a href='#' class='close'>&times;</a>" +
                "<h3>" + heading + "</h3>" +
                "</div>" +
                "<div class='modal-body'></div>" +
                "<div class='modal-footer'></div>" +
                "</div>");
        if (bodyClass != null) {
            modal.find(".modal-body").addClass(bodyClass);
        }
        $("div.book").before(modal);
        var body = modal.find(".modal-body");
        elements.appendTo(body);
        body.find("a").click(function() { modal.modal("hide"); });
    };

    //
    var addModalToBar = function(id, heading) {
        $("<li>" +
                "<a href='#' data-controls-modal='" + id + "' data-backdrop='true' data-keyboard='true'>" + heading + "</a>" +
                "</li>").appendTo("#topbar");
    };

    //
    var title = $("div.titlepage h1.title");
    if (title.length == 1) {
        var titleLink = $("<a class='brand' href='#'></a>").appendTo("#topbar");
        titleLink.text(title.text());
    }

    // Table of Content
    if ($('div.book > div.toc').length == 1) {
        addModal("modal-toc", "Table of Content", $("div.book > div.toc > ul").addClass("unstyled").clone(), "modal-overflow");
        addModalToBar("modal-toc", "Table of Content");
        $(".toc").remove();
    }

    // Lit of Figures
    if ($('div.book > div.list-of-figures').length == 1) {
        addModal("modal-figures", "List of Figures", $("div.book > div.list-of-figures > ul").addClass("unstyled").clone(), "modal-overflow");
        addModalToBar("modal-figures", "List of Figures");
        $(".list-of-figures").remove();
    }

    // List of Tables
    if ($('div.book > div.list-of-tables').length == 1) {
        addModal("modal-tables", "List of Tables", $("div.book > div.list-of-tables > ul").addClass("unstyled").clone(), "modal-overflow");
        addModalToBar("modal-tables", "List of Tables");
        $(".list-of-tables").remove();
    }

    // List of Examles
    if ($('div.book > div.list-of-examples').length == 1) {
        addModal("modal-examples", "List of Examples", $("div.book > div.list-of-examples > ul").addClass("unstyled").clone(), "modal-overflow");
        addModalToBar("modal-examples", "List of Examples");
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

    // Callout
    $(".calloutlist").each(function() {
        var ol = $("<ol></ol>").appendTo($(this));
        $(this).find("tr").each(function() {
            var o = $(this).find("td:eq(1)").html();
            $("<li></li>").appendTo(ol).html(o);
        });
        $(this).find("> table").remove();
    });
});
