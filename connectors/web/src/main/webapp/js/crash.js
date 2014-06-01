
//
CRaSH = (function(element, width, height) {

  //
  width = width || 1024;
  height = height || 768;

  // Constant
  var cls = "\033[2J\033[1;1H";

  //
  var log = {
    error: function(message) { console.error(message); },
    debug: function(message) { console.debug(message); },
    info: function(message) { console.info(message); },
    trace: function(message) { console.trace(message); }
  };

  //
  var welcome = true;
  var socket = null;
  var completion = null;
  var cancelTime = 0;
  var executing = false;

  //
  var terminal = element.terminal(function (command, term) {
    if (socket != null) {
      var event = {
        type: "execute",
        width: term.cols() - 2, // We need -2 to compensate resize when scrollbar appears to preserve formatting
        height: term.rows(),
        command: command
      };
      socket.send(JSON.stringify(event));
      executing = true;
      term.pause();
    } else {
      log.debug("Could not execute command " + command + " because of null socket");
    }
  }, {
    greetings: '',
    name: 'CRaSH',
    enabled: false,
    width: width,
    height: height,
    prompt: '',
    tabcompletion: true,
    completion: function(term, string, callback) {
      var prefix = term.get_command();
      if (socket != null) {
        completion = {
          callback: callback,
          prefix: prefix,
          string: string
        };
        socket.send(JSON.stringify({type: "complete",prefix:prefix}));
        term.pause();
      } else {
        log.debug("Could not perform completion of " + prefix + " because of null socket");
      }
    },
    keypress: function(event, term) {
      if (executing) {
        var code = event.keyCode;
        if (code == 3) {
          log.debug("Cancelling current command");
          cancelTime = (new Date()).getTime();
          socket.send(JSON.stringify({type: "cancel"}));
        } else {
          log.debug("Sending key event " + code);
          socket.send(JSON.stringify({type: "key",keyType:"character",keyCode:code}));
        }
        return false;
      } else {
        return true;
      }
    },
    keydown: function(event, term) {
      if (executing) {
        var keyType;
        switch (event.keyCode) {
          case 38:
            keyType = "up";
            break;
          case 40:
            keyType = "down";
            break;
          case 37:
            keyType = "left";
            break;
          case 39:
            keyType = "right";
            break;
          case 13:
            keyType = "enter";
            break;
          default:
            log.debug("Unmapped key code " + event.keyCode);
            return;
        }
        log.debug("Sending key event " + keyType);
        socket.send(JSON.stringify({type: "key",keyType: keyType}));
        return false;
      }
    }
  });

  //
  return {

    pause: function() {
      terminal.pause();
    },

    resume: function() {
      terminal.resume();
    },

    connect: function(host) {
      if (socket == null) {
        if ('WebSocket' in window) {
          log.debug("Creating WebSocket");
          socket = new WebSocket(host);
        } else if ('MozWebSocket' in window) {
          log.debug("Creating WebSocket");
          socket = new MozWebSocket(host);
        } else {
          log.error('WebSocket is not supported by this browser.');
          return;
        }

        //
        socket.onopen = function () {
          log.debug('WebSocket connection opened');
          terminal.resume();
          if (welcome) {
            log.debug("Asking welcome");
            welcome = false;
            socket.send(JSON.stringify({type: "welcome"}));
          } else {
            log.debug("Reconnected");
          }
        };
        var self = this;
        socket.onclose = function () {
          log.debug('WebSocket closed');
          terminal.pause();
          socket = null;
          // We may be executing and having connection closed => that would create an UI bug
          executing = false;
          // If we had a recent cancel, close can be due to session trashed by the server
          // so we reconnect quickly so user won't notice the difference
          var currentTime = (new Date()).getTime();
          var waitTime;
          var delta = currentTime - cancelTime;
          if (delta < 5000) {
            waitTime = 100;
          } else {
            waitTime = 5000;
          }
          setTimeout(function(){
            // We reconnect
            log.debug("Reconnecting");
            self.connect(host);
          }, waitTime);
        };
        socket.onmessage = function (message) {
          var event = $.parseJSON(message.data);
          var type = event.type;
          if (type == "print") {
            var text = event.data;
            log.debug("Printing to term");
            log.trace("Message : <" + text + ">");
            while (true) {
              var index = text.indexOf(cls);
              if (index == -1) {
                break;
              } else {
                terminal.echo(text.substring(0, index));
                terminal.clear();
                text = text.substring(index + cls.length, text.length);
              }
            }
            terminal.echo(text);
          } else if (type == "prompt") {
            log.debug("Updating prompt");
            terminal.set_prompt(event.data);
          } else if (type == "end") {
            log.debug("Ending command");
            executing = false;
            terminal.resume();
          } else if (type == "complete") {
            log.debug("Completing completion");
            var completions = event.data;
            for (var i = 0; i < completions.length; i++) {
              completions[i] = completion.string + completions[i];
            }
            completion.callback(completions);
            terminal.resume();
          }
        };
      } else {
        log.error("Already connected");
      }
    },
    close: function() {
      // Todo
    }
  };
});