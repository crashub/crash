context.takeAlternateBuffer();
for (int i = 0;i < 10;i++) {
  out.cls();
  out << "Time is " + i + "\n";
  out.flush();
  Thread.sleep(1000);
}
