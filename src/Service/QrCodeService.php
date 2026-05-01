<?php
// src/Service/QrCodeService.php
namespace App\Service;

use Endroid\QrCode\QrCode;
use Endroid\QrCode\Writer\SvgWriter;

class QrCodeService
{
    public function generateQrCode(string $data): string
    {
        $qrCode = new QrCode($data);
        $writer = new SvgWriter();
        $result = $writer->write($qrCode);
        return $result->getString();
    }
}