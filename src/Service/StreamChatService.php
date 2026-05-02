<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\Utilisateur;
use GetStream\StreamChat\Client as StreamClient;
use GetStream\StreamChat\StreamException;
use GuzzleHttp\Client as GuzzleClient;

class StreamChatService
{
    private StreamClient $client;

    public function __construct()
    {
        $apiKey = $_ENV['STREAM_API_KEY'] ?? getenv('STREAM_API_KEY');
        $apiSecret = $_ENV['STREAM_API_SECRET'] ?? getenv('STREAM_API_SECRET');

        if (!$apiKey || !$apiSecret) {
            throw new \RuntimeException('Stream Chat API credentials are missing.');
        }

        $this->client = new StreamClient($apiKey, $apiSecret);

        // Development only: disable SSL verification
        $guzzleClient = new GuzzleClient([
            'verify' => false,
            'timeout' => 30.0,
        ]);

        $this->client->setHttpClient($guzzleClient);
    }

    public function upsertUser(Utilisateur $user): void
    {
        $this->client->upsertUser([
            'id' => (string) $user->getId(),
            'name' => $user->getUserIdentifier(),
            'role' => 'user',
        ]);
    }

    public function generateToken(Utilisateur $user): string
    {
        return $this->client->createToken((string) $user->getId());
    }

    /**
     * @param string[] $participantIds
     *
     * @return array{id: string, type: string}
     */
    public function getOrCreateEventChannel(Event $event, array $participantIds = []): array
    {
        $channelId = 'event_' . $event->getId_event();
        $channel = $this->client->channel('messaging', $channelId);

        $creator = !empty($participantIds) ? $participantIds[0] : 'system';

        $channel->create($creator, $participantIds);

        return [
            'id' => $channelId,
            'type' => 'messaging',
        ];
    }

    public function addUserToEventChannel(Event $event, Utilisateur $user): void
    {
        $this->upsertUser($user);

        $userId = (string) $user->getId();
        $channelId = 'event_' . $event->getId_event();
        $channel = $this->client->channel('messaging', $channelId);

        if (!$this->channelExists($channelId)) {
            $channel->create($userId, [$userId]);
            return;
        }

        $channel->addMembers([$userId]);
    }

    private function channelExists(string $channelId): bool
    {
        try {
            $response = $this->client->queryChannels([
                'id' => $channelId,
            ]);

            $channels = $response['channels'] ?? [];

            return count($channels) > 0;
        } catch (StreamException $e) {
            return false;
        }
    }

    /**
     * @return array<int, array{
     *     id: string,
     *     event_id: string,
     *     name: mixed,
     *     last_message: mixed,
     *     member_count: int
     * }>
     */
    public function getUserChannels(Utilisateur $user): array
    {
        $filter = [
            'type' => 'messaging',
            'members' => [
                '$in' => [(string) $user->getId()],
            ],
        ];

        $response = $this->client->queryChannels($filter);
        $channels = $response['channels'] ?? [];

        $result = [];

        foreach ($channels as $channel) {
            $channelId = $channel['id'] ?? '';

            if (!is_string($channelId) || strpos($channelId, 'event_') !== 0) {
                continue;
            }

            $eventId = str_replace('event_', '', $channelId);

            if (!is_numeric($eventId)) {
                continue;
            }

            $result[] = [
                'id' => $channelId,
                'event_id' => $eventId,
                'name' => $channel['name'] ?? $channelId,
                'last_message' => $channel['last_message']['text'] ?? '',
                'member_count' => count($channel['members'] ?? []),
            ];
        }

        return $result;
    }
}