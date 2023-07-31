# Inhaltsverzeichnis
 - [Encoder](#encoder)
    - [Socket und dynamische Port Nummer](#socket-und-dynamische-port-nummer)
    - [Daten aus Java](#daten-aus-java)
    - [Nachricht zurück an Java](#nachricht-zurück-an-java)
 - [Decoder](#decoder)
    - [Socket und Port](#socket-und-port)
    - [Empfangen der Nachricht von Java](#empfangen-der-nachricht-von-java)
    - [Daten zurück nach Java](#daten-zurück-nach-java)
        - [Umformen der Daten](#umformen-der-daten)
        - [Runden der Daten](#runden-der-daten)
        - [Zurücksenden](#zurücksenden)




# Encoder
Der Encoder soll Daten aus Java empfangen, verarbeiten und danach die encodierten Daten mit oder ohne Gewichte wieder nach Java zurückschicken. 

## Socket und dynamische Port Nummer

Es wird ein TCP Socket aufgesetzt und mithilfe der Kommandozeile kann ein Port festgelegt werden. Wenn kein Port über die Kommandozeile übergeben wurde, wird der Standart Port **3141** verwendet. Die Ressourcen werden nach dem Verlassen des **with** Blocks wieder geschlossen. 

```python
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as my_socket:
        try:
            try:
                port_number = int(sys.argv[1])
            except IndexError:
                port_number = 3141  # Standard Port Number

            my_socket.bind(("localhost", port_number))
            my_socket.listen()

        except OSError:
            print("Something went wrong while Setting up Sockets")
```

## Daten aus Java

Die gesendeten Daten aus Java werden vom Socket akzeptiert und mit **recv** in die Variable **data** gespeichert. Da die gesendeten Bytes im JSON Format sind, kann dieser direkt geladen werden. Danach folgen weitere Schritte zur Verarbeitung.
Bei den empfangenen Bytes handelt es sich um 5 Zeilen von Wetterdaten.

```python
        while True:
            try:
                conn, addr = my_socket.accept()

                data = conn.recv(1024)

                data_as_string = json.loads(data)

                # Ab hier ist Code zur Verarbeitung der empfangenen Daten 

            except OSError:
                print("Something went wrong while getting and sending data...")
                print("Closing Socket now...")
                break

```
Bei der Verarbeitung der Daten werden die gesendeten Daten umgeformt (reshaped), sortiert und die kleinste Grid Nummer ausgelesen. Danach werden die Daten encodiert.


## Nachricht zurück an Java

Nachdem die Daten encodiert wurden und entschieden wurde, ob die Gewichte nun mitgeschickt werden sollen müssen einige Vorbereitungen getroffen werden für das Zurücksenden der Nachricht. 

Aufbau der Nachricht, die nach Java und von dort zum Decoder geschickt wird:

    Länge der Nachricht  |   JSON Array


Bestimmung der Länge der Nachricht:

    Nachrichtenlänge = Länge des JSON Arrays + Trennungszeichen + Länge des Strings der Nachrichtenlänge

```python
   message_length = len(sending_message_as_json) + len("|")
   message_length += len(str(message_length))
```

Bedeutung des Trennungszeichens   "**|**" :

An dem Trennungszeichen wird im Decoder die gesendeten Bytes getrennt, um die Nachrichtenlänge herauszufinden, da Python nur eine Byte Länge von 8192 pro recv maximal auf einmal empfangen kann. Aus diesem Grund müssen wir solange recv im Decoder aufrufen, bis die komplette Nachrichtenlänge empfangen wurde. Wenn wir die Länge nicht mitschicken weiß der Decoder nicht, wie lang die Nachricht ist und würde zu oft recv aufrufen, was zum Einfrieren des Sockets führen würde.

 
Aufbau des JSON Array:

     [ Gewichte? Ja: **1** Nein: **0** ,   Kleinste Grid Nummer ,   Encodierten Daten,   Gewichte (wenn sie gesendet werden)  ]


Der dazugehörige Python Code:
```python

if composed_autoencoder.better_than_reference(data_elem, factor): 
  # Wenn Gewichte mitgesendet werden sollen

  encoded_data = encoder.predict(data_elem)
  encoded_data_as_json = json.dumps(encoded_data.tolist())
  weights_as_json = weigths_to_json(get_weights(composed_autoencoder.get_decoder()))

  sending_message_as_json = json.dumps([1, smallest_grid_number, encoded_data_as_json, weights_as_json])

  message_length = len(sending_message_as_json) + len("|")
  message_length += len(str(message_length))

  sending_message = bytes(str(message_length) + "|" + sending_message_as_json + "\n", encoding="utf-8")
  conn.sendall(sending_message)

else:

  encoded_data = encoder.predict(data_elem)
  encoded_data_as_json = json.dumps(encoded_data.tolist())
 
  sending_message_as_json = json.dumps([0, smallest_grid_number, encoded_data_as_json])

  message_length = len(sending_message_as_json) + len("|")
  message_length += len(str(message_length))

  sending_message = bytes(str(message_length) + "|" + sending_message_as_json + "\n", encoding="utf-8")

  conn.sendall(sending_message)
```
Die Nachricht selbst muss in Bytes versendet werden, wodurch die Nachricht mit der Methode **bytes() **von einem String in Bytes umgewandelt wird mit einem encoding im UTF8 Format. Mit **conn.sendall(sending_message)** wird sichergestellt, dass alles direkt gesendet wird.

# Decoder
Nachdem die Nachricht vom Encoder über Java zum Decoder gesendet wurde, wird im Decoder die Nachricht über ein TCP Socket empfangen, decodiert, gerundet und nach Java wieder zurückgeschickt.

## Socket und Port

Ähnlich wie beim Encoder wird auch wieder ein TCP Socket verwendet, nur das hier kein Port über die Kommandozeile übergeben wird, sondern der Port **3142** verwendet wird. Auch hier wird ein **with** zum Schließen der Ressourcen verwendet.

```python
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as my_socket:
        try:
            my_socket.bind(("localhost", 3142))
            my_socket.listen()
        except OSError:
            print("Something went wrong while Setting up Sockets")
```

## Empfangen der Nachricht von Java

Wenn Gewichte mitgeschickt werden, ist die Nachricht deutlich größer als 8192 (>65000), wodurch mehr als einmal **recv** aufgerufen werden muss. Aus diesem Grund wird am Anfang der gesendeten Nachricht die Länge der Nachricht und ein Trennzeichen mitgeschickt, damit wir genau wissen, wie oft recv aufgerufen werden muss.

Zuerst akzeptieren wir die Verbindung und rufen einmal **recv** auf. Wir teilen die gesendeten Daten am Trennzeichen mit der **split** Methode auf. Damit ist in **send_data[0]** die Nachrichtenlänge in Bytes und in **send_data[1]** ein Teil des JSON Arrays. Damit die restlichen Daten nun auch empfangen werden, müssen wir die Nachrichtenlänge in einen Integer umwandeln und von ihr das Trennzeichen und die Länge des Strings der Nachrichtenlänge abziehen, um auf die Länge des JSON Arrays zu kommen.

Um nun die restlichen Daten zu erhalten wird eine **while** Schleife durchlaufen, die überprüft, ob die Länge des gespeicherten JSON Arrays schon der gesamten Länge entspricht. Wenn nicht, wird **recv** aufgerufen und weitere Daten drangehängt.

Wenn das JSON Array komplett empfangen wurde, kann dieses geladen und die Daten ausgelesen werden.

```python 
        while True:
            try:
                conn, addr = my_socket.accept()

                send_data = conn.recv(8192)
                send_data = send_data.split(b'|')
                message_length = int(send_data[0]) - len("|") - len(send_data[0])

                data = send_data[1]

                while len(data) < message_length:
                    send_data = conn.recv(8192)
                    data += send_data

                data = json.loads(data)
                weights_send = bool(data[0])
                smallest_grid_number = data[1]

                # Hier folgt die Verarbeitung der Daten

            except OSError:
                print("Something went wrong while getting and sending data...")
                print("Closing Socket now...")
                break
```

## Daten zurück nach Java

Nachdem die Daten decodiert wurden, sollen sie wieder nach Java im richtigem Format geschickt werden.

### Umformen der Daten
Die decodierten Daten müssen wieder in n Zeilen umgewandelt werden. Aus diesem Grund wird die **reshape** Methode vom Numpy Array aufgerufen.

```python 
 # Reshape the data set to n = number_of_line lines
 decoded_data = decoded_data.reshape((number_of_lines, -1))
```

### Runden der Daten

Die decodierten Daten sind Fließkommazahlen (float) und sollen für die Übertragung am Besten auf 3 Nachkommastellen gerundet werden. Die Gridnummer hingegen muss wieder in ein Integer umgewandelt werden. 

Folgender Code übernimmt das Runden der decodierten Daten:

```python 
# Round every Value of the decoded Data
  for i in range(len(decoded_data)):
      decoded_data[i][0] = int(round(decoded_data[i][0], 0))
      for j in range(1, len(decoded_data[i])):
           decoded_data[i][j] = round(decoded_data[i][j], 3)
```
Das i der äußeren Schleife gibt das i-te Element in der Liste an, also die jeweilige Zeile. In den Zeilen ist das erste Element die Gridnummer, welche auf 0 Stellen gerundet und in ein Integer umgewandelt wird. Das j der inneren Schleife durchläuft die restlichen Elemente der Zeile und rundet diese auf 3 Stellen.

### Zurücksenden

Nachdem die Daten gerundet wurden, können sie wieder an Java geschickt werden, indem die Daten wieder in Bytes umgewandelt werden im UTF8 Format. Auch hier sorgt das **sendall** dafür, dass alles direkt geschickt wird.

```python 
decoded_data = json.dumps(decoded_data)

decoded_data_as_bytes = bytes(decoded_data + "\n", encoding="utf-8")

conn.sendall(decoded_data_as_bytes)

```


