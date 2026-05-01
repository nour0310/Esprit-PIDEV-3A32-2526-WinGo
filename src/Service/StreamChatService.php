<?php

namespace App\Service;

use App\Entity\Event;
use App\Entity\Utilisateur;
use GetStream\StreamChat\Client as StreamClient;
use GuzzleHttp\Client as GuzzleClient;
use GetStream\StreamChat\StreamException;

class StreamChatService
{
    private StreamClient $client;
    private string $apiKey;

    public function __construct()
    {
        $apiKey = $_ENV['STREAM_API_KEY'] ?? getenv('STREAM_API_KEY');
        $apiSecret = $_ENV['STREAM_API_SECRET'] ?? getenv('STREAM_API_SECRET');

        if (!$apiKey || !$apiSecret) {
            throw new \RuntimeException('Stream Chat API credentials are missing.');
        }

        $this->apiKey = $apiKey;
        $this->client = new StreamClient($apiKey, $apiSecret);

        // Development only: disable SSL verification
        $guzzleClient = new GuzzleClient(['verify' => false, 'timeout' => 30.0]);
        $this->client->setHttpClient($guzzleClient);
    }

    public function upsertUser(Utilisateur $user): void
    {
        $this->client->upsertUser([
            'id'   => (string) $user->getId(),
            'name' => $user->getUserIdentifier(),
            'role' => 'user',
        ]);
    }

    public function generateToken(Utilisateur $user): string
    {
        return $this->client->createToken((string) $user->getId());
    }

    /**
     * Create or get an existing event group channel.
     * No custom data to avoid SDK bug.
     */
    public function getOrCreateEventChannel(Event $event, array $participantIds = []): array
    {
        $channelId = 'event_' . $event->getId_event();
        $channel = $this->client->channel('messaging', $channelId);
        $creator = !empty($participantIds) ? $participantIds[0] : 'system';
        // Create channel without any custom data
        $channel->create($creator, $participantIds);
        return ['id' => $channelId, 'type' => 'messaging'];
    }

    /**
     * Add a user to an event channel (when they register)
     */
    public function addUserToEventChannel(Event $event, Utilisateur $user): void
    {
        // 1. Ensure the user exists in Stream
        $this->upsertUser($user);

        $userId = (string) $user->getId();
        $channelId = 'event_' . $event->getId_event();
        $channel = $this->client->channel('messaging', $channelId);

        // 2. Check if channel already exists
        $exists = $this->channelExists($channelId);

        if (!$exists) {
            // Create the channel with the current user as first member
            $channel->create($userId, [$userId]);
        } else {
            // Channel exists, just add the user
            $channel->addMembers([$userId]);
        }
    }

    /**
     * Vérifie si un canal existe déjà dans Stream.
     */
    private function channelExists(string $channelId): bool
    {
        try {
            $response = $this->client->queryChannels([
                'id' => $channelId,
            ]);
            $channels = $response['channels'] ?? $response ?? [];
            return count($channels) > 0;
        } catch (StreamException $e) {
            // En cas d'erreur, on considère qu'il n'existe pas
            return false;
        }
    }

    public function getUserChannels(Utilisateur $user): array
    {
        $filter = ['type' => 'messaging', 'members' => ['$in' => [(string) $user->getId()]]];
        $response = $this->client->queryChannels($filter);
        $channels = $response['channels'] ?? $response ?? [];

        $result = [];
        foreach ($channels as $channel) {
            $channelId = $channel['id'] ?? '';
            // Only include channels that start with 'event_'
            if (strpos($channelId, 'event_') !== 0) {
                continue;
            }
            $eventId = str_replace('event_', '', $channelId);
            if (!is_numeric($eventId)) {
                continue;
            }
            $result[] = [
                'id'           => $channelId,
                'event_id'     => $eventId,
                'name'         => $channel['name'] ?? $channelId,
                'last_message' => $channel['last_message']['text'] ?? '',
                'member_count' => count($channel['members'] ?? []),
            ];
        }
        return $result;
    }
}