<?php

namespace App\Service;

class AuthPageTranslationService
{
    /**
     * @var array<string, array<string, string>>
     */
    private const LOGIN_FALLBACKS = [
        'FR' => [
            'page_title' => 'WinGo - Connexion',
            'title' => 'Bienvenue !',
            'subtitle' => 'Connectez-vous a votre compte',
            'email' => 'Adresse email',
            'email_placeholder' => 'votre@email.com',
            'password' => 'Mot de passe',
            'remember_me' => 'Se souvenir de moi',
            'captcha' => 'Code de securite (Captcha)',
            'captcha_placeholder' => 'Entrez le code',
            'submit' => 'Se connecter',
            'forgot_password' => 'Mot de passe oublie ?',
            'face_id' => 'Connexion par Face ID (IA)',
            'no_account' => 'Pas encore de compte ?',
            'register' => "S'inscrire",
            'language' => 'Langue',
        ],
        'EN' => [
            'page_title' => 'WinGo - Login',
            'title' => 'Welcome!',
            'subtitle' => 'Sign in to your account',
            'email' => 'Email address',
            'email_placeholder' => 'your@email.com',
            'password' => 'Password',
            'remember_me' => 'Remember me',
            'captcha' => 'Security code (Captcha)',
            'captcha_placeholder' => 'Enter the code',
            'submit' => 'Log in',
            'forgot_password' => 'Forgot password?',
            'face_id' => 'Login with Face ID (AI)',
            'no_account' => "Don't have an account yet?",
            'register' => 'Register',
            'language' => 'Language',
        ],
        'AR' => [
            'page_title' => 'WinGo - تسجيل الدخول',
            'title' => 'مرحبا!',
            'subtitle' => 'سجل الدخول إلى حسابك',
            'email' => 'البريد الإلكتروني',
            'email_placeholder' => 'your@email.com',
            'password' => 'كلمة المرور',
            'remember_me' => 'تذكرني',
            'captcha' => 'رمز الأمان',
            'captcha_placeholder' => 'أدخل الرمز',
            'submit' => 'تسجيل الدخول',
            'forgot_password' => 'هل نسيت كلمة المرور؟',
            'face_id' => 'تسجيل الدخول عبر Face ID',
            'no_account' => 'ليس لديك حساب؟',
            'register' => 'إنشاء حساب',
            'language' => 'اللغة',
        ],
    ];

    /**
     * @var array<string, array<string, string>>
     */
    private const REGISTER_FALLBACKS = [
        'FR' => [
            'page_title' => 'WinGo - Inscription',
            'title' => 'Creer un compte',
            'subtitle' => 'Rejoignez la communaute WinGo',
            'first_name' => 'Prenom *',
            'first_name_placeholder' => 'Votre prenom',
            'last_name' => 'Nom *',
            'last_name_placeholder' => 'Votre nom',
            'email' => 'Adresse email *',
            'email_placeholder' => 'votre@email.com',
            'password' => 'Mot de passe *',
            'phone' => 'Telephone',
            'phone_placeholder' => '12345678',
            'age' => 'Age',
            'age_placeholder' => '25',
            'captcha' => 'Code de securite (Captcha)',
            'captcha_placeholder' => 'Entrez le code',
            'submit' => 'Creer mon compte',
            'has_account' => 'Deja un compte ?',
            'login' => 'Se connecter',
            'language' => 'Langue',
        ],
        'EN' => [
            'page_title' => 'WinGo - Register',
            'title' => 'Create an account',
            'subtitle' => 'Join the WinGo community',
            'first_name' => 'First name *',
            'first_name_placeholder' => 'Your first name',
            'last_name' => 'Last name *',
            'last_name_placeholder' => 'Your last name',
            'email' => 'Email address *',
            'email_placeholder' => 'your@email.com',
            'password' => 'Password *',
            'phone' => 'Phone',
            'phone_placeholder' => '12345678',
            'age' => 'Age',
            'age_placeholder' => '25',
            'captcha' => 'Security code (Captcha)',
            'captcha_placeholder' => 'Enter the code',
            'submit' => 'Create my account',
            'has_account' => 'Already have an account?',
            'login' => 'Log in',
            'language' => 'Language',
        ],
        'AR' => [
            'page_title' => 'WinGo - إنشاء حساب',
            'title' => 'إنشاء حساب',
            'subtitle' => 'انضم إلى مجتمع WinGo',
            'first_name' => 'الاسم *',
            'first_name_placeholder' => 'الاسم',
            'last_name' => 'اللقب *',
            'last_name_placeholder' => 'اللقب',
            'email' => 'البريد الإلكتروني *',
            'email_placeholder' => 'your@email.com',
            'password' => 'كلمة المرور *',
            'phone' => 'الهاتف',
            'phone_placeholder' => '12345678',
            'age' => 'العمر',
            'age_placeholder' => '25',
            'captcha' => 'رمز الأمان',
            'captcha_placeholder' => 'أدخل الرمز',
            'submit' => 'إنشاء الحساب',
            'has_account' => 'لديك حساب بالفعل؟',
            'login' => 'تسجيل الدخول',
            'language' => 'اللغة',
        ],
    ];

    public function __construct(
        private readonly DeepLTranslationService $deepLTranslationService
    ) {
    }

    /**
     * @return array<string, string>
     */
    public function forLogin(string $lang): array
    {
        return $this->resolve('LOGIN', $lang, self::LOGIN_FALLBACKS);
    }

    /**
     * @return array<string, string>
     */
    public function forRegister(string $lang): array
    {
        return $this->resolve('REGISTER', $lang, self::REGISTER_FALLBACKS);
    }

    /**
     * @return array<string, string>
     */
    public function forForgotPassword(string $lang): array
    {
        return $this->resolve('FORGOT_PASSWORD', $lang, [
            'FR' => [
                'page_title' => 'WinGo - Mot de passe oublie',
                'title' => 'Mot de passe oublie ?',
                'subtitle' => 'Entrez votre adresse email pour recevoir un lien de reinitialisation.',
                'email' => 'Adresse email',
                'email_placeholder' => 'ex: jean@email.com',
                'submit' => 'Envoyer le lien',
                'back_to_login' => 'Connexion',
                'remember' => 'Se souvenir du mot de passe ?',
                'language' => 'Langue',
            ],
            'EN' => [
                'page_title' => 'WinGo - Forgot Password',
                'title' => 'Forgot your password?',
                'subtitle' => 'Enter your email address to receive a reset link.',
                'email' => 'Email address',
                'email_placeholder' => 'e.g. john@email.com',
                'submit' => 'Send reset link',
                'back_to_login' => 'Login',
                'remember' => 'Remember your password?',
                'language' => 'Language',
            ],
            'AR' => [
                'page_title' => 'WinGo - نسيت كلمة المرور',
                'title' => 'هل نسيت كلمة المرور؟',
                'subtitle' => 'أدخل بريدك الإلكتروني للحصول على رابط إعادة التعيين.',
                'email' => 'البريد الإلكتروني',
                'email_placeholder' => 'example@email.com',
                'submit' => 'إرسال الرابط',
                'back_to_login' => 'تسجيل الدخول',
                'remember' => 'تتذكر كلمة المرور؟',
                'language' => 'اللغة',
            ],
        ]);
    }

    /**
     * @return array<string, string>
     */
    public function forResetPassword(string $lang): array
    {
        return $this->resolve('RESET_PASSWORD', $lang, [
            'FR' => [
                'page_title' => 'WinGo - Reinitialiser le mot de passe',
                'title' => 'Choisissez votre nouveau mot de passe',
                'password' => 'Nouveau mot de passe',
                'hint' => 'Min. 8 caracteres, une majuscule, une minuscule et un chiffre.',
                'submit' => 'Mettre a jour le mot de passe',
                'language' => 'Langue',
            ],
            'EN' => [
                'page_title' => 'WinGo - Reset Password',
                'title' => 'Choose your new password',
                'password' => 'New password',
                'hint' => 'At least 8 characters, one uppercase letter, one lowercase letter and one number.',
                'submit' => 'Update password',
                'language' => 'Language',
            ],
            'AR' => [
                'page_title' => 'WinGo - إعادة تعيين كلمة المرور',
                'title' => 'اختر كلمة المرور الجديدة',
                'password' => 'كلمة المرور الجديدة',
                'hint' => '8 أحرف على الأقل، حرف كبير، حرف صغير، ورقم واحد.',
                'submit' => 'تحديث كلمة المرور',
                'language' => 'اللغة',
            ],
        ]);
    }

    /**
     * @return array<string, string>
     */
    public function forCheckEmail(string $lang): array
    {
        return $this->resolve('CHECK_EMAIL', $lang, [
            'FR' => [
                'page_title' => 'WinGo - Verifiez Votre Email',
                'title' => 'Verifiez votre boite mail',
                'text_1' => 'Si un compte existe avec cette adresse email, un lien de reinitialisation a ete envoye.',
                'text_2' => 'Pensez aussi a verifier votre dossier spam ou courrier indesirable.',
                'back_to_login' => 'Retour a la connexion',
                'language' => 'Langue',
            ],
            'EN' => [
                'page_title' => 'WinGo - Check Your Email',
                'title' => 'Check your inbox',
                'text_1' => 'If an account exists with this email address, a reset link has been sent.',
                'text_2' => 'Also remember to check your spam or junk folder.',
                'back_to_login' => 'Back to login',
                'language' => 'Language',
            ],
            'AR' => [
                'page_title' => 'WinGo - تحقق من بريدك الإلكتروني',
                'title' => 'تحقق من بريدك الإلكتروني',
                'text_1' => 'إذا كان هناك حساب مرتبط بهذا البريد الإلكتروني، فقد تم إرسال رابط إعادة التعيين.',
                'text_2' => 'لا تنس التحقق من مجلد الرسائل غير المرغوب فيها.',
                'back_to_login' => 'العودة إلى تسجيل الدخول',
                'language' => 'اللغة',
            ],
        ]);
    }

    public function normalizeLang(?string $lang): string
    {
        $lang = strtoupper(trim((string) $lang));

        return match ($lang) {
            'AR', 'EN', 'FR' => $lang,
            default => 'FR',
        };
    }

    /**
     * @param array<string, array<string, string>> $fallbacks
     * @return array<string, string>
     */
    private function resolve(string $page, string $lang, array $fallbacks): array
    {
        $lang = $this->normalizeLang($lang);

        if ($lang === 'FR') {
            return $fallbacks['FR'];
        }

        try {
            $translated = [];
            foreach ($fallbacks['FR'] as $key => $text) {
                $translated[$key] = $this->deepLTranslationService->translate($text, $lang, 'FR')['text'];
            }

            return $translated;
        } catch (\Throwable) {
            return $fallbacks[$lang] ?? $fallbacks['FR'];
        }
    }
}
