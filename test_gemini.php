<?php
$key = 'AIzaSyDMVZPz1vjvXaUQxfQCqe-Mpssh0HT8qj0';
$url = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=' . $key;
$data = ['contents' => [['parts' => [['text' => 'Hi']]]]];
$options = [
    'http' => [
        'header'  => "Content-type: application/json\r\n",
        'method'  => 'POST',
        'content' => json_encode($data),
        'ignore_errors' => true
    ]
];
$context  = stream_context_create($options);
$result = file_get_contents($url, false, $context);
echo "Result:\n" . substr($result, 0, 100) . "\n";
