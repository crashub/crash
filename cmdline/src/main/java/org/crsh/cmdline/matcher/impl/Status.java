package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.*;
import org.crsh.cmdline.matcher.tokenizer.Token;
import org.crsh.cmdline.matcher.tokenizer.Tokenizer;

import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class Status {

  /**
   * The input.
   */
  static class Request {

    /** . */
    final Mode mode;
    
    /** . */
    final String mainName;
    
    /** . */
    Tokenizer tokenizer;

    /** . */
    final CommandDescriptor<?, ?> command;

    Request(Mode mode, String mainName, Tokenizer tokenizer, CommandDescriptor<?, ?> command) {
      this.mode = mode;
      this.mainName = mainName;
      this.tokenizer = tokenizer;
      this.command = command;
    }
  }

  /**
   * The output.
   */
  static class Response {

    /** . */
    Status status;

    /** . */
    LinkedList<Event> events;

    /** . */
    CommandDescriptor<?, ?> command;

    Response(Status status) {
      this.status = status;
      this.events = null;
      this.command = null;
    }

    Response() {
      this.status = null;
      this.events = null;
      this.command = null;
    }

    void add(Event event) {
      if (events == null) {
        events = new LinkedList<Event>();
      }
      events.add(event);
    }

    void addAll(Collection<Event> toAdd) {
      if (events == null) {
        events = new LinkedList<Event>();
      }
      events.addAll(toAdd);
    }
  }

  /**
   * Process a request.
   *
   * @param req the request
   * @param <T> the generic type of the command
   * @return the response
   */
  abstract <T> Response process(Request req);

  static class ReadingOption extends Status {

    <T> Response process(Request req) {
      Response response = new Response();
      Token token = req.tokenizer.peek();
      if (token == null) {
        response.add(new Event.Stop.Done.Option(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {
        Token.Literal literal = (Token.Literal)token;
        if (literal instanceof Token.Literal.Option) {
          Token.Literal.Option optionToken = (Token.Literal.Option)literal;
          if (optionToken.getName().length() == 0 && optionToken instanceof Token.Literal.Option.Long) {
            req.tokenizer.next();
            if (req.tokenizer.hasNext()) {
              if (req.command instanceof ClassDescriptor<?>) {
                ClassDescriptor<T> classCommand = (ClassDescriptor<T>)req.command;
                MethodDescriptor<T> m = classCommand.getMethod(req.mainName);
                if (m != null) {
                  response.command = m;
                  response.add(new Event.Method.Implicit(m, optionToken));
                }
              }
              response.status = new Status.WantReadArg();
            } else {
              if (req.mode == Mode.INVOKE) {
                if (req.command instanceof ClassDescriptor<?>) {
                  ClassDescriptor<T> classCommand = (ClassDescriptor<T>)req.command;
                  MethodDescriptor<T> m = classCommand.getMethod(req.mainName);
                  if (m != null) {
                    response.command = m;
                    response.add(new Event.Method.Implicit(m, optionToken));
                  }
                }
                response.status = new Status.Done();
                response.add(new Event.Stop.Done.Arg(req.tokenizer.getIndex()));
              } else {
                if (req.command instanceof ClassDescriptor<?>) {
                  ClassDescriptor<T> classCommand = (ClassDescriptor<T>)req.command;
                  MethodDescriptor<T> m = classCommand.getMethod(req.mainName);
                  if (m != null) {
                    response.command = m;
                    response.add(new Event.Method.Implicit(m, optionToken));
                    response.add(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
                  } else  {
                    response.add(new Event.Stop.Unresolved.NoSuchOption.Class(optionToken));
                  }
                } else {
                  response.add(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
                }
              }
            }
          } else {
            OptionDescriptor<?> desc = req.command.findOption(literal.getValue());
            if (desc != null) {
              req.tokenizer.next();
              int arity = desc.getArity();
              LinkedList<Token.Literal.Word> values = new LinkedList<Token.Literal.Word>();
              while (arity > 0) {
                if (req.tokenizer.hasNext()) {
                  Token a = req.tokenizer.peek();
                  if (a instanceof Token.Whitespace) {
                    req.tokenizer.next();
                    if (req.tokenizer.hasNext() && req.tokenizer.peek() instanceof Token.Literal.Word) {
                      // ok
                    } else {
                      req.tokenizer.pushBack();
                      break;
                    }
                  } else {
                    Token.Literal b = (Token.Literal)a;
                    if (b instanceof Token.Literal.Word) {
                      values.addLast((Token.Literal.Word)b);
                      req.tokenizer.next();
                      arity--;
                    } else {
                      req.tokenizer.pushBack();
                      break;
                    }
                  }
                } else {
                  break;
                }
              }
              response.add(new Event.Option(desc, optionToken, values));
            } else {
              // We are reading an unknown option
              // it could match an option of an implicit command
              if (req.command instanceof ClassDescriptor<?>) {
                MethodDescriptor<T> m = ((ClassDescriptor<T>)req.command).getMethod(req.mainName);
                if (m != null) {
                  desc = m.findOption(literal.getValue());
                  if (desc != null) {
                    response.command = m;
                    response.add(new Event.Method.Implicit(m, literal));
                  } else {
                    if (req.command.getOptionNames().size() == 0) {
                      response.command = m;
                      response.add(new Event.Method.Implicit(m, literal));
                    } else {
                      response.add(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
                    }
                  }
                } else {
                  response.add(new Event.Stop.Unresolved.NoSuchOption.Class(optionToken));
                }
              } else {
                response.add(new Event.Stop.Unresolved.NoSuchOption.Method(optionToken));
              }
            }
          }
        } else {
          Token.Literal.Word wordLiteral = (Token.Literal.Word)literal;
          if (req.command instanceof ClassDescriptor<?>) {
            ClassDescriptor<T> classCommand = (ClassDescriptor<T>)req.command;
            MethodDescriptor<T> m = classCommand.getMethod(wordLiteral.getValue());
            if (m != null && !m.getName().equals(req.mainName)) {
              response.command = m;
              req.tokenizer.next();
              response.add(new Event.Method.Explicit(m, wordLiteral));
            } else {
              m = classCommand.getMethod(req.mainName);
              if (m != null) {
                response.add(new Event.Method.Implicit(m, wordLiteral));
                response.status = new Status.WantReadArg();
                response.command = m;
              } else {
                response.status = new Status.WantReadArg();
              }
            }
          } else {
            response.status = new Status.WantReadArg();
          }
        }
      }
      return response;
    }

  }

  static class WantReadArg extends Status {
    @Override
    <T> Response process(Request req) {
      switch (req.mode) {
        case INVOKE:
          return new Response(new Status.ComputeArg());
        case COMPLETE:
          return new Response(new Status.ReadingArg());
        default:
          throw new AssertionError();
      }
    }
  }

  static class ComputeArg extends Status {

    @Override
    <T> Response process(Request req) {
      Token token = req.tokenizer.peek();
      Response response = new Response();
      if (token == null) {
        response.add(new Event.Stop.Done.Arg(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {

        //
        List<? extends ArgumentDescriptor<?>> arguments = req.command.getArguments();

        // Count the number ok remaining non whitespace;
        int tokenCount = 0;
        int wordCount = 0;
        do {
          Token t = req.tokenizer.next();
          if (t instanceof Token.Literal) {
            wordCount++;
          }
          tokenCount++;
        }
        while (req.tokenizer.hasNext());
        req.tokenizer.pushBack(tokenCount);

        //
        int oneCount = 0;
        int zeroOrOneCount = 0;
        int index = 0;
        for (ArgumentDescriptor<?> argument : arguments) {
          Multiplicity multiplicity = argument.getMultiplicity();
          if (multiplicity == Multiplicity.SINGLE) {
            if (argument.isRequired()) {
              if (oneCount + 1 > wordCount) {
                break;
              }
              oneCount++;
            } else {
              zeroOrOneCount++;
            }
          }
          index++;
        }

        // This the number of arguments we can satisfy
        arguments = arguments.subList(0, index);

        // How many words we can consume for zeroOrOne and zeroOrMore
        int toConsume = wordCount - oneCount;

        // Correct the zeroOrOneCount and adjust toConsume
        zeroOrOneCount = Math.min(zeroOrOneCount, toConsume);
        toConsume -= zeroOrOneCount;

        // The remaining
        LinkedList<Event> events = new LinkedList<Event>();
        for (ArgumentDescriptor<?> argument : arguments) {
          int size;
          switch (argument.getMultiplicity()) {
            case SINGLE:
              if (argument.isRequired()) {
                size = 1;
              } else {
                if (zeroOrOneCount > 0) {
                  zeroOrOneCount--;
                  size = 1;
                } else {
                  size = 0;
                }
              }
              break;
            case MULTI:
              // We consume the remaining
              size = toConsume;
              toConsume = 0;
              break;
            default:
              throw new AssertionError();
          }

          // Now take care of the argument
          if (size > 0) {
            List<Token.Literal> values = new ArrayList<Token.Literal>(size);
            while (size > 0) {
              Token t = req.tokenizer.next();
              if (t instanceof Token.Literal) {
                values.add(((Token.Literal)t));
                size--;
              }
            }
            events.addLast(new Event.Argument(argument, values));

            // Add the whitespace if needed
            if (req.tokenizer.hasNext() && req.tokenizer.peek() instanceof Token.Whitespace) {
              events.addLast(new Event.Separator((Token.Whitespace) req.tokenizer.next()));
            }
          }
        }

        //
        events.addLast(new Event.Stop.Done.Arg(req.tokenizer.getIndex()));

        //
        response.status = new Status.Done();
        response.addAll(events);
      }
      return response;
    }
  }

  static class Done extends Status {
    @Override
    <T> Response process(Request req) {
      throw new IllegalStateException();
    }
  }

  static class ReadingArg extends Status {

    /** . */
    private final int index;

    ReadingArg() {
      this(0);
    }

    private ReadingArg(int index) {
      this.index = index;
    }

    ReadingArg next() {
      return new ReadingArg(index + 1);
    }

    @Override
    <T> Response process(Request req) {
      Token token = req.tokenizer.peek();
      Response response = new Response();
      if (token == null) {
        response.add(new Event.Stop.Done.Arg(req.tokenizer.getIndex()));
      } else if (token instanceof Token.Whitespace) {
        response.add(new Event.Separator((Token.Whitespace) token));
        req.tokenizer.next();
      } else {
        final Token.Literal literal = (Token.Literal)token;
        List<? extends ArgumentDescriptor<?>> arguments = req.command.getArguments();
        if (index < arguments.size()) {
          ArgumentDescriptor<?> argument = arguments.get(index);
          switch (argument.getMultiplicity()) {
            case SINGLE:
              req.tokenizer.next();
              response.add(new Event.Argument(argument, Arrays.asList(literal)));
              response.status = next();
              break;
            case MULTI:
              req.tokenizer.next();
              List<Token.Literal> values = new ArrayList<Token.Literal>();
              values.add(literal);
              while (req.tokenizer.hasNext()) {
                Token capture = req.tokenizer.next();
                if (capture instanceof Token.Literal) {
                  values.add(((Token.Literal)capture));
                } else {
                  if (req.tokenizer.hasNext()) {
                    // Ok
                  } else {
                    req.tokenizer.pushBack();
                    break;
                  }
                }
              }
              response.add(new Event.Argument(argument, values));
          }
        } else {
          response.add(new Event.Stop.Unresolved.TooManyArguments(literal));
        }
      }
      return response;
    }
  }
}
